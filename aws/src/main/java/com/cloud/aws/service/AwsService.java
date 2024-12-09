package com.cloud.aws.service;

import com.cloud.aws.dto.request.CreateInstanceRequest;
import com.cloud.aws.dto.request.GetCondorStatusRequest;
import com.cloud.aws.dto.request.GetInstanceIdRequest;
import com.cloud.aws.dto.response.CondorStatusResponse;
import com.cloud.aws.dto.response.ImageResponse;
import com.cloud.aws.dto.response.RegionResponse;
import com.cloud.aws.dto.response.ZoneResponse;
import com.cloud.aws.model.AwsInstance;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AwsService {

    private final Ec2Client ec2Client;
    private static final String  privateKeyPath = "/Users/sinseung-yong/cloud-aws.pem";
    private static final String username = "ec2-user";

    public List<AwsInstance> listInstances() {
        try {
            DescribeInstancesRequest request = DescribeInstancesRequest.builder().build();
            List<Instance> instances = ec2Client.describeInstances(request)
                    .reservations()
                    .stream()
                    .flatMap(reservation -> reservation.instances().stream())
                    .toList();

            return instances.stream().map(
                    instance -> AwsInstance.create(
                            instance.instanceId(),
                            instance.imageId(),
                            instance.instanceType().toString(),
                            instance.state().nameAsString(),
                            instance.monitoring().stateAsString(),
                            instance.publicDnsName()
                    )
            ).toList();
        } catch (AwsServiceException e) {
            throw new RuntimeException(e);
        } catch (SdkClientException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ZoneResponse> listAvailableZones() {
        try {
            DescribeAvailabilityZonesRequest request = DescribeAvailabilityZonesRequest.builder().build();
            List<AvailabilityZone> availabilityZones = ec2Client.describeAvailabilityZones(request).availabilityZones();
            return availabilityZones.stream()
                    .map(availabilityZone -> ZoneResponse.builder()
                            .zoneId(availabilityZone.zoneId())
                            .zoneRegion(availabilityZone.regionName())
                            .zoneRegionName(availabilityZone.zoneName())
                            .build())
                    .toList();
        } catch (AwsServiceException e) {
            throw new RuntimeException(e);
        } catch (SdkClientException e) {
            throw new RuntimeException(e);
        }
    }

    public void startInstance(GetInstanceIdRequest getInstanceIdRequest) {
        try {
            StartInstancesRequest request = StartInstancesRequest.builder()
                    .instanceIds(getInstanceIdRequest.instanceId())
                    .build();
            ec2Client.startInstances(request);
        } catch (AwsServiceException e) {
            throw new RuntimeException(e);
        } catch (SdkClientException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopInstance(GetInstanceIdRequest getInstanceIdRequest) {
        try {
            StopInstancesRequest request = StopInstancesRequest.builder()
                    .instanceIds(getInstanceIdRequest.instanceId())
                    .build();
            ec2Client.stopInstances(request);
        } catch (AwsServiceException e) {
            throw new RuntimeException(e);
        } catch (SdkClientException e) {
            throw new RuntimeException(e);
        }
    }

    public List<RegionResponse> listAvailableRegions() {
        try {
            DescribeRegionsRequest request = DescribeRegionsRequest.builder().build();
            DescribeRegionsResponse response = ec2Client.describeRegions(request);
            return response.regions().stream()
                    .map(region -> RegionResponse.builder()
                            .regionName(region.regionName())
                            .regionEndpoint(region.endpoint())
                            .build())
                    .toList();
        } catch (AwsServiceException e) {
            throw new RuntimeException(e);
        } catch (SdkClientException e) {
            throw new RuntimeException(e);
        }
    }

    public void rebootInstance(GetInstanceIdRequest getInstanceIdRequest) {
        try {
            RebootInstancesRequest request = RebootInstancesRequest.builder()
                    .instanceIds(getInstanceIdRequest.instanceId())
                    .build();
            RebootInstancesResponse response = ec2Client.rebootInstances(request);
            log.debug("Rebooting instance: {}", response.toString());
        } catch (AwsServiceException e) {
            throw new RuntimeException(e);
        } catch (SdkClientException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ImageResponse> listImages() {
        try {
            DescribeImagesRequest request = DescribeImagesRequest.builder()
                    .owners("self")
                    .build();
            DescribeImagesResponse response = ec2Client.describeImages(request);
            return response.images().stream()
                    .map(image -> ImageResponse.builder()
                            .imageId(image.imageId())
                            .imageName(image.name())
                            .imageDescription(image.imageTypeAsString())
                            .build())
                    .toList();
        } catch (AwsServiceException e) {
            throw new RuntimeException(e);
        } catch (SdkClientException e) {
            throw new RuntimeException(e);
        }
    }

    public String createInstance(CreateInstanceRequest createInstanceRequest) {
        try {
            RunInstancesRequest request = RunInstancesRequest.builder()
                    .imageId(createInstanceRequest.instanceAmi())
                    .instanceType(InstanceType.T2_MICRO)
                    .minCount(1)
                    .maxCount(1)
                    .securityGroups("HTCondor")
                    .keyName("cloud-aws")
                    .build();
            return ec2Client.runInstances(request).instances().get(0).instanceId();
        } catch (AwsServiceException e) {
            throw new RuntimeException(e);
        } catch (SdkClientException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean startCondorOnAws(GetCondorStatusRequest request) {
        try {
            // SSH 키 설정
            JSch jsch = new JSch();
            jsch.addIdentity(privateKeyPath);

            // 세션 설정
            Session session = jsch.getSession(username, request.host(), 22);
            session.setConfig("StrictHostKeyChecking", "no"); // 호스트 키 확인 비활성화
            session.connect();

            // 명령 실행
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("sudo su -c 'systemctl start condor'");
            channel.setErrStream(System.err);

            InputStream in = channel.getInputStream();
            channel.connect();

            // 결과 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }

            // 채널 종료
            channel.disconnect();
            session.disconnect();

            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to start condor on AWS instance", e);
        }
    }

    public CondorStatusResponse executeCondorStatusOnAws(GetCondorStatusRequest request) {
        StringBuilder output = new StringBuilder();

        JSch jsch = new JSch();
        try {
            // SSH 키 설정
            jsch.addIdentity(privateKeyPath);

            // 세션 설정
            Session session = jsch.getSession(username, request.host(), 22);
            session.setConfig("StrictHostKeyChecking", "no"); // 호스트 키 확인 비활성화
            session.connect();

            // 명령 실행
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("condor_status");
            channel.setErrStream(System.err);

            InputStream in = channel.getInputStream();
            channel.connect();

            // 결과 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // 채널 종료
            channel.disconnect();
            session.disconnect();

        } catch (Exception e) {
            throw new RuntimeException("Failed to execute condor_status on AWS instance", e);
        }

        return CondorStatusResponse.builder()
                .result(output.toString())
                .build();
    }
}

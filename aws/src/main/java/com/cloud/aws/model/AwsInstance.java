package com.cloud.aws.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class AwsInstance {
    private String instanceId;
    private String instanceAmi;
    private String instanceType;
    private String instanceState;
    private String instanceMonitoringState;
    private String instancePublicDnsName;

    public static AwsInstance create (String instanceId, String instanceAmi, String instanceType, String instanceState, String instanceMonitoringState, String instancePublicDnsName) {
        return AwsInstance.builder()
                .instanceId(instanceId)
                .instanceAmi(instanceAmi)
                .instanceType(instanceType)
                .instanceState(instanceState)
                .instanceMonitoringState(instanceMonitoringState)
                .instancePublicDnsName(instancePublicDnsName)
                .build();
    }
}

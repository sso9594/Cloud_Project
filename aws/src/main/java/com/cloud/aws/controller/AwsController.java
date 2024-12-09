package com.cloud.aws.controller;

import com.cloud.aws.dto.request.CreateInstanceRequest;
import com.cloud.aws.dto.request.GetCondorStatusRequest;
import com.cloud.aws.dto.request.GetInstanceIdRequest;
import com.cloud.aws.dto.response.CondorStatusResponse;
import com.cloud.aws.dto.response.ImageResponse;
import com.cloud.aws.dto.response.RegionResponse;
import com.cloud.aws.dto.response.ZoneResponse;
import com.cloud.aws.model.AwsInstance;
import com.cloud.aws.service.AwsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/aws")
@Validated
public class AwsController {

    private final AwsService awsService;

    @GetMapping("/instances")
    public ResponseEntity<List<AwsInstance>> getInstanceList() {
        return ResponseEntity.ok(awsService.listInstances());
    }

    @GetMapping("/zones")
    public ResponseEntity<List<ZoneResponse>> getAvailableZones() {
        return ResponseEntity.ok(awsService.listAvailableZones());
    }

    @GetMapping("/regions")
    public ResponseEntity<List<RegionResponse>> getAvailableRegions() {
        return ResponseEntity.ok(awsService.listAvailableRegions());
    }

    @PostMapping("/start")
    public ResponseEntity<String> startInstance(@RequestBody GetInstanceIdRequest request) {
        awsService.startInstance(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stop")
    public ResponseEntity<Void> stopInstance(@RequestBody GetInstanceIdRequest request) {
        awsService.stopInstance(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reboot")
    public ResponseEntity<Void> rebootInstance(@RequestBody GetInstanceIdRequest request) {
        awsService.rebootInstance(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createInstance(@RequestBody CreateInstanceRequest createInstanceRequest) {
        awsService.createInstance(createInstanceRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/images")
    public ResponseEntity<List<ImageResponse>> listImages() {
        return ResponseEntity.ok(awsService.listImages());
    }

    @PostMapping("/condor/start")
    public ResponseEntity<Boolean> startCondor(@RequestBody GetCondorStatusRequest request) {
        return ResponseEntity.ok(awsService.startCondorOnAws(request));
    }

    @PostMapping("/condor/status")
    public ResponseEntity<CondorStatusResponse> getCondorStatus(@RequestBody GetCondorStatusRequest request) {
        return ResponseEntity.ok(awsService.executeCondorStatusOnAws(request));
    }
}

package com.cloud.aws.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegionResponse {
    private String regionName;
    private String regionEndpoint;

    public RegionResponse(String regionName, String regionEndpoint) {
        this.regionName = regionName;
        this.regionEndpoint = regionEndpoint;
    }
}


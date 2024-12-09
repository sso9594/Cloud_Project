package com.cloud.aws.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ZoneResponse {

    private String zoneId;
    private String zoneRegion;
    private String zoneRegionName;

    public ZoneResponse(String zoneId, String zoneRegion, String zoneRegionName) {
        this.zoneId = zoneId;
        this.zoneRegion = zoneRegion;
        this.zoneRegionName = zoneRegionName;
    }
}

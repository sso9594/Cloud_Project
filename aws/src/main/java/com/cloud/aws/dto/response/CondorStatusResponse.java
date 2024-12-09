package com.cloud.aws.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CondorStatusResponse {
    private String result;

    public CondorStatusResponse(String result) {
        this.result = result;
    }
}

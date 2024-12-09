package com.cloud.aws.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageResponse {
    private String imageId;
    private String imageName;
    private String imageDescription;

    public ImageResponse(String imageId, String imageName, String imageDescription) {
        this.imageId = imageId;
        this.imageName = imageName;
        this.imageDescription = imageDescription;
    }
}

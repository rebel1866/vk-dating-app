package com.melnikov.service.vo.betafaceapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class ImageUploadRequest {
    @JsonProperty("api_key")
    private String apiKey;
    @JsonProperty("file_uri")
    private String fileUri;
    @JsonProperty("detection_flags")
    private String detectionFlags;
    @JsonProperty("original_filename")
    private String originalFileName;

    public ImageUploadRequest(String apiKey, String detectionFlags) {
        this.apiKey = apiKey;
        this.detectionFlags = detectionFlags;
    }
}

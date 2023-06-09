package com.melnikov.service.vo.betafaceapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
public class RecognizeRequestVo {
    @JsonProperty("api_key")
    private String apiKey;
    @JsonProperty("faces_uuids")
    private List<String> facesUids;
    private List<String> targets;
}

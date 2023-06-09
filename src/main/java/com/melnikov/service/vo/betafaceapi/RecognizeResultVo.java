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
public class RecognizeResultVo {
    @JsonProperty("face_uuid")
    private String faceUid;
    private List<MatchVo> matches;
}

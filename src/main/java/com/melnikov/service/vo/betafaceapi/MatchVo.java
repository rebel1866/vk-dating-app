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
public class MatchVo {
    @JsonProperty("face_uuid")
    private String faceUid;
    private Double confidence;
    @JsonProperty("is_match")
    private Boolean isMatch;
    @JsonProperty("person_id")
    private String personId;
}

package com.melnikov.service.vo.betafaceapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class TagVo {
    private String name;
    private String value;
    private Double confidence;
}

package com.melnikov.service.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class CareerVo {
    @JsonProperty("city_id")
    private Integer cityId;
    private String company;
    private String position;
}

package com.melnikov.service.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class PersonalVo {
    private Integer alcohol;
    @JsonProperty("inspired_by")
    private String inspiredBy;
    @JsonProperty("life_main")
    private Integer lifeMain;
    @JsonProperty("people_main")
    private Integer peopleMain;
    private Integer political;
    private Integer smoking;
    private String religion;

}

package com.melnikov.dao.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserDescription {
    private String interests;
    private String books;
    private String quotes;
    private String about;
    private String movies;
    private String activities;
    private String music;
    private String mobilePhone;
    private String universityName;
    private String facultyName;
    private String peopleMain;
    private String political;
    private String smoking;
    private String religion;
    private String alcohol;
    private String inspiredBy;
    private String lifeMain;
    private String relation;
    private List<Career> careers;
}

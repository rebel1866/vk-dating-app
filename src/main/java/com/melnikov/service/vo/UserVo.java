package com.melnikov.service.vo;

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
public class UserVo {
    private Long id;
    private String bdate;
    private CityVo city;
    @JsonProperty("is_friend")
    private Boolean isFriend;
    private String interests;
    private String books;
    private String quotes;
    private String about;
    private String movies;
    private String activities;
    private String music;
    @JsonProperty("can_write_private_message")
    private Boolean canWritePrivateMessage;
    @JsonProperty("can_send_friend_request")
    private Boolean canSendFriendRequest;
    @JsonProperty("mobile_phone")
    private String mobilePhone;
    @JsonProperty("last_seen")
    private LastSeenVo lastSeen;
    @JsonProperty("is_favorite")
    private Boolean isFavorite;
    private List<CareerVo> career;
    @JsonProperty("university_name")
    private String universityName;
    @JsonProperty("faculty_name")
    private String facultyName;
    private Integer relation;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("is_closed")
    private Boolean isClosed;
    private PersonalVo personal;
    @JsonProperty("has_photo")
    private Boolean hasPhoto;
    //instagram
}

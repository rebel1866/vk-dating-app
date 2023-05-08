package com.melnikov.dao.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.melnikov.service.vo.CareerVo;
import com.melnikov.service.vo.CityVo;
import com.melnikov.service.vo.LastSeenVo;
import com.melnikov.service.vo.PersonalVo;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document("users")
public class User {
    @Id
    private Long id;
    private String bdate;
    private String cityName;
    private String firstName;
    private String lastName;
    private Short age;
    private Boolean isFriend;           // upd
    private Boolean canWritePrivateMessage; // upd update these fields when they're fetched from db
    private Boolean canSendFriendRequest; //upd
    private LocalDateTime savingTime;
    private Boolean hasBeenViewed;
    private Boolean isVkFavorite;
    private Boolean isApplicationFavorite;
    private UserDescription userDescription;
    private List<Photo> photos;
}

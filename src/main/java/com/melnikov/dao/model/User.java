package com.melnikov.dao.model;

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
@Document(collection = "users")
public class User {
    @Id
    private Long id;
    private String bdate;
    private String cityName;
    private String firstName;
    private String lastName;
    private Short age;
    private Boolean isFriend;
    private Boolean canWritePrivateMessage;
    private Boolean canSendFriendRequest;
    private LocalDateTime savingTime;
    private Boolean hasBeenViewed;
    private Boolean isVkFavorite;
    private Boolean isApplicationFavorite;
    private UserDescription userDescription;
    private List<Photo> photos;
}

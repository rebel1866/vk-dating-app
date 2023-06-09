package com.melnikov.dao.model;

import lombok.*;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserAppearance {
    private Boolean isAttractive;
    private Double attractivenessConfidence;
    private Boolean isBlond;
    private List<Match> matches;
}

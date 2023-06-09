package com.melnikov.dao.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Match {
    private String targetNameSpace;
    private Boolean isMatch;
    private Double matchConfidence;
}

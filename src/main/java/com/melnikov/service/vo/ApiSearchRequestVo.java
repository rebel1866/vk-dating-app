package com.melnikov.service.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ApiSearchRequestVo {
    private String q;
    private Byte birthDay;
    private Byte birthMonth;
}

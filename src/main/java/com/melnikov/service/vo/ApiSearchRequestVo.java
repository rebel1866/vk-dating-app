package com.melnikov.service.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class ApiSearchRequestVo {
    private String q;
    private Byte birthDay;
    private Byte birthMonth;
}

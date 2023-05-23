package com.melnikov.service.vo;

import lombok.ToString;

import java.util.List;

@ToString
public class UserGetVoWrapper {
    private List<UserVo> response;

    public List<UserVo> getResponse() {
        return response;
    }

    public void setResponse(List<UserVo> response) {
        this.response = response;
    }
}

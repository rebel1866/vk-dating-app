package com.melnikov.service.vo;

import lombok.ToString;

@ToString
public class SearchUserResponseWrapperVo<T> {
    private SearchUserResponseVo<T> response;

    public SearchUserResponseVo<T> getResponse() {
        return response;
    }

    public void setResponse(SearchUserResponseVo<T> response) {
        this.response = response;
    }
}

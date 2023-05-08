package com.melnikov.service.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchUserResponseVo {
    private Integer count;
    private List<UserVo> items;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<UserVo> getItems() {
        return items;
    }

    public void setItems(List<UserVo> items) {
        this.items = items;
    }
}

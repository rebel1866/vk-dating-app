package com.melnikov.service.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.ToString;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class SearchUserResponseVo<T> {
    private Integer count;
    private List<T> items;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}

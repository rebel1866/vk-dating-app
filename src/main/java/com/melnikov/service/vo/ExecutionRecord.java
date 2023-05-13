package com.melnikov.service.vo;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionRecord {
    private Boolean isSuccess;
    private Boolean isEmptyResponse;
    private Integer amountAddedUsers;
    private ApiSearchRequestVo requestVo;
    private List<String> comments = new ArrayList<>();

    public ExecutionRecord(Boolean isSuccess, Boolean isEmptyResponse, Integer amountAddedUsers, ApiSearchRequestVo requestVo) {
        this.isSuccess = isSuccess;
        this.isEmptyResponse = isEmptyResponse;
        this.amountAddedUsers = amountAddedUsers;
        this.requestVo = requestVo;
    }

    @Override
    public String toString() {
        return "ExecutionRecord{" +
                "isSuccess=" + isSuccess +
                ", isEmptyResponse=" + isEmptyResponse +
                ", amountAddedUsers=" + amountAddedUsers +
                ", requestVo=" + requestVo +
                ", comments=" + comments +
                '}';
    }
}

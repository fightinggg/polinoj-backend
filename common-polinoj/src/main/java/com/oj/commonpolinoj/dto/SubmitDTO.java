package com.oj.commonpolinoj.dto;

import lombok.Data;

@Data
public class SubmitDTO {
    Long id;
    Long problemId;
    String sourceSubmitId;
    String user;
    Integer status;
    String code;
    String submitTime;
    Long execTime;
    Long execMemory;
}

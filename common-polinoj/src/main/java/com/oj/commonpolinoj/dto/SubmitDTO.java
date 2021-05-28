package com.oj.commonpolinoj.dto;

import lombok.Data;

@Data
public class SubmitDTO {
    private Long id;
    private Long problemId;
    private String sourceSubmitId;
    private String code;
    private Long userId;
    private Integer status;
    private Long execTime;
    private Long execMemory;
    private String ccInfo;
    private String runInfo;
    private Long contextId;
    private Long submitTime;
}

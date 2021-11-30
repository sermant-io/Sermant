package com.huawei.javamesh.backend.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Result {

    private String errorCode;

    private String errorMsg;

    private String hint;

    /**
     * 业务ID，实际情况可能会变动
     */
    private Long businessId;
}

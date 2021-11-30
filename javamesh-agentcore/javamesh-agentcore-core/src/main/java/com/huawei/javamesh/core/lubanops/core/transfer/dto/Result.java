package com.huawei.javamesh.core.lubanops.core.transfer.dto;

import com.alibaba.fastjson.JSON;

public class Result {

    private String errorCode;

    private String errorMsg;

    private String hint;

    /**
     * 业务ID，实际情况可能会变动
     */
    private Long businessId;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}

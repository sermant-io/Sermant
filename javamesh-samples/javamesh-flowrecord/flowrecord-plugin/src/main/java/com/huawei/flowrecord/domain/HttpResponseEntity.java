package com.huawei.flowrecord.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpResponseEntity {
    /**
     * 返回状态码
     */
    private int status;

    /**
     * 响应体对象
     */
    private Object httpResponseBody;
}

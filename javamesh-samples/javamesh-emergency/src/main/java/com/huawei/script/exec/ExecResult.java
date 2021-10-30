/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec;

import lombok.Data;

/**
 * 脚本执行的结果
 *
 * @author y30010171
 * @since 2021-10-20
 **/
@Data
public class ExecResult {
    private static final int FAIL_CODE = -1;
    private int code;
    private String msg;

    public static ExecResult success(String msg) {
        ExecResult execResult = new ExecResult();
        execResult.setCode(0);
        execResult.setMsg(msg);
        return execResult;
    }

    public static ExecResult fail(String msg) {
        ExecResult execResult = new ExecResult();
        execResult.setCode(FAIL_CODE);
        execResult.setMsg(msg);
        return execResult;
    }

    public boolean isSuccess() {
        return code == 0;
    }
}

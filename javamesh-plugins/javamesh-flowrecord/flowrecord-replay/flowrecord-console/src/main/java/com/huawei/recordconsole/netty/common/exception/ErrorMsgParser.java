/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.recordconsole.netty.common.exception;

/**
 * 错误信息处理器
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-07-12
 */
public interface ErrorMsgParser {
    /**
     * 处理错误信息
     *
     * @param args 参数
     * @return 处理后的错误信息
     */
    String parseErrorMsg(Object args);
}

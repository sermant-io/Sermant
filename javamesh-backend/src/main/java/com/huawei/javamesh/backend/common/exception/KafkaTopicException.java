/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.javamesh.backend.common.exception;

import java.util.Locale;

/**
 * kafka主题异常类
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
 */
public class KafkaTopicException extends RuntimeException {
    public static final ErrorMsgParser TOPIC_NOT_EXISTS = args -> String.format(Locale.ROOT,
        "These topics %s don't exist.", args);

    public KafkaTopicException(ErrorMsgParser parser, String args) {
        parser.parseErrorMsg(args);
    }
}

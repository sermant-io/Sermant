/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.sermant.backend.common.exception;

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

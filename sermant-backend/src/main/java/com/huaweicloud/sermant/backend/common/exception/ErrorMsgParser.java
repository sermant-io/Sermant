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

package com.huaweicloud.sermant.backend.common.exception;

/**
 * 错误信息处理器
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-08-07
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

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

package com.huawei.javamesh.core.lubanops.integration.transport.http;

import java.io.UnsupportedEncodingException;

/**
 * @author
 * @date 2020/8/7 15:36
 */
public interface Request {

    /**
     * 设置签名。
     *
     * @param signature 签名数据
     */
    void setSignature(String signature);

    /**
     * 生成原生request对象。
     *
     * @return
     * @throws
     */
    Object generate() throws UnsupportedEncodingException;

}

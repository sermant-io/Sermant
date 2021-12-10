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

package com.huawei.javamesh.core.lubanops.integration.exception;

/**
 * 这种类型异常主要是开发用户不遵循规划或者数据库系统有脏数据或者系统配置错误等情况下抛出， 抛出这种异常代表系统出问题 <br>
 *
 * @author
 * @since 2020年3月1日
 */
public class ShouldNotHappenException extends JavaagentRuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 5363743637146702849L;

    public ShouldNotHappenException() {
    }

    public ShouldNotHappenException(String message) {
        super(message);
    }

}

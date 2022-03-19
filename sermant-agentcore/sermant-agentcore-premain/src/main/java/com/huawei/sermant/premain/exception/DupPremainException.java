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

package com.huawei.sermant.premain.exception;

/**
 * 重复执行premain方法异常
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/12
 */
public class DupPremainException extends RuntimeException {
    private static final long serialVersionUID = -7867167604375273742L;

    public DupPremainException() {
        super("Unable to execute sermant agent duplicated. ");
    }
}

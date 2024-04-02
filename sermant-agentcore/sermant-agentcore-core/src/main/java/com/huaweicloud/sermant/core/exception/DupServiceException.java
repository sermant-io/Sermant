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

package com.huaweicloud.sermant.core.exception;

import java.util.Locale;

/**
 * Duplicate service exception
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-17
 */
public class DupServiceException extends RuntimeException {
    private static final long serialVersionUID = 126761488232028879L;

    /**
     * constructor
     *
     * @param clsName clsName
     */
    public DupServiceException(String clsName) {
        super(String.format(Locale.ROOT, "Found more than one implement of %s. ", clsName));
    }
}

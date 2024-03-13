/*
 *  Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.sermant.database.prohibition.integration.constant;

/**
 * common constant
 *
 * @author daizhenyu
 * @since 2024-03-11
 **/
public class DatabaseConstant {
    /**
     * fail to execute database write operation status code
     */
    public static final String OPERATION_FAIL_CODE = "100";

    /**
     * succeed to execute database write operation status code
     */
    public static final String OPERATION_SUCCEED_CODE = "101";

    /**
     * database table data count
     */
    public static final String DATA_COUNT = "1";

    /**
     * line separator
     */
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private DatabaseConstant() {
    }
}

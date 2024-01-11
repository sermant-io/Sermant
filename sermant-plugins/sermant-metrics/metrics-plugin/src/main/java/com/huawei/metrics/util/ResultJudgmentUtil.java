/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.metrics.util;

import com.huawei.metrics.common.Constants;
import com.huawei.metrics.common.ResultType;

/**
 * 结果判断工具类
 *
 * @author zhp
 * @since 2023-12-15
 */
public class ResultJudgmentUtil {
    private ResultJudgmentUtil() {
    }

    /**
     * 判断HTTP的请求结果
     *
     * @param statusCode 结果编码
     * @return 结果类型
     */
    public static int judgeHttpResult(int statusCode) {
        if (statusCode <= Constants.MAX_SUCCESS_CODE) {
            return ResultType.SUCCESS.getValue();
        }
        if (statusCode <= Constants.MAX_CLIENT_ERROR_CODE) {
            return ResultType.CLIENT_ERROR.getValue();
        }
        if (statusCode <= Constants.MAX_SERVER_ERROR_CODE) {
            return ResultType.SERVER_ERROR.getValue();
        }
        return ResultType.ERROR.getValue();
    }

    /**
     * 判断Dubbo的请求结果
     *
     * @param status 结果编码
     * @return 结果类型
     */
    public static int judgeDubboResult(byte status) {
        if (status == Constants.DUBBO_OK) {
            return ResultType.SUCCESS.getValue();
        }
        for (int i = 0; i < Constants.DUBBO_CLIENT_ERROR.length; i++) {
            if (status == Constants.DUBBO_CLIENT_ERROR[i]) {
                return ResultType.CLIENT_ERROR.getValue();
            }
        }
        for (int i = 0; i < Constants.DUBBO_SERVER_ERROR.length; i++) {
            if (status == Constants.DUBBO_SERVER_ERROR[i]) {
                return ResultType.SERVER_ERROR.getValue();
            }
        }
        return ResultType.ERROR.getValue();
    }

    /**
     * 判断Mysql的请求结果
     *
     * @param status 结果编码
     * @return 结果类型
     */
    public static int judgeMysqlResult(int status) {
        if (status >= Constants.MYSQL_CLIENT_ERROR[0][0] && status <= Constants.MYSQL_CLIENT_ERROR[0][1]
                || status >= Constants.MYSQL_CLIENT_ERROR[1][0] && status <= Constants.MYSQL_CLIENT_ERROR[1][1]) {
            return ResultType.CLIENT_ERROR.getValue();
        }
        if (status >= Constants.MYSQL_SERVER_ERROR[0][0] && status <= Constants.MYSQL_SERVER_ERROR[0][1]
                || status >= Constants.MYSQL_SERVER_ERROR[1][0] && status <= Constants.MYSQL_SERVER_ERROR[1][1]) {
            return ResultType.SERVER_ERROR.getValue();
        }
        return ResultType.ERROR.getValue();
    }
}

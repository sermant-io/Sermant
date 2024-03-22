/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.sermant.database.prohibition.handler;

import com.huaweicloud.sermant.database.prohibition.common.constant.DatabaseConstant;
import com.huaweicloud.sermant.database.prohibition.entity.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

/**
 * global exception handling
 *
 * @author zhp
 * @since 2024-01-13
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * handler SqlException
     *
     * @param sqlException sql Exception
     * @return result information
     */
    @ExceptionHandler(SQLException.class)
    public Result handlerSqlException(SQLException sqlException) {
        log.warn(sqlException.getMessage(), sqlException);
        if (sqlException.getMessage().startsWith(DatabaseConstant.SQL_EXCEPTION_MESSAGE_PREFIX)) {
            return new Result(DatabaseConstant.OPERATION_FAIL_CODE, sqlException.getMessage(), null);
        }
        return new Result(DatabaseConstant.OPERATION_SUCCEED_CODE, sqlException.getMessage(), null);
    }
}
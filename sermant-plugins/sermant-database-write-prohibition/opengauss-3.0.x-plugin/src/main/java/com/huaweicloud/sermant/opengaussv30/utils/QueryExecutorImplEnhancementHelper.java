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

package com.huaweicloud.sermant.opengaussv30.utils;

import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;
import com.huaweicloud.sermant.database.handler.DatabaseHandler;
import com.huaweicloud.sermant.opengaussv30.interceptors.QueryExecutorImplInterceptor;

/**
 * postgresql拦截点辅助类
 *
 * @author zhp
 * @since 2024-02-04
 **/
public class QueryExecutorImplEnhancementHelper {
    private static final String EXECUTE_METHOD_NAME = "sendQuery";

    private static final String ENHANCE_CLASS_NAME = "org.opengauss.core.v3.QueryExecutorImpl";

    private static final String INT_CLASS_NAME = "int";

    private static final String QUERY_CLASS_NAME = "org.opengauss.core.Query";

    private static final String V3_PARAMETER_LIST_CLASS_NAME = "org.opengauss.core.v3.V3ParameterList";

    private static final String RESULT_HANDLER_CLASS_NAME = "org.opengauss.core.ResultHandler";

    private static final String BATCH_RESULT_HANDLER_CLASS_NAME = "org.opengauss.jdbc.BatchResultHandler";

    private static final String[] EXECUTE_INTERNAL_METHOD_PARAMS_TYPE = {
            QUERY_CLASS_NAME,
            V3_PARAMETER_LIST_CLASS_NAME,
            INT_CLASS_NAME,
            INT_CLASS_NAME,
            INT_CLASS_NAME,
            RESULT_HANDLER_CLASS_NAME,
            BATCH_RESULT_HANDLER_CLASS_NAME
    };

    private QueryExecutorImplEnhancementHelper() {
    }

    private static MethodMatcher getSendQueryMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_METHOD_NAME)
                .and(MethodMatcher.paramTypesEqual(EXECUTE_INTERNAL_METHOD_PARAMS_TYPE));
    }

    /**
     * 获取QueryExecutorImpl sendQuery方法有参拦截声明器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer QueryExecutorImpl sendQuery方法有参拦截声明器
     */
    public static InterceptDeclarer getSendQueryInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getSendQueryMethodMatcher(), new QueryExecutorImplInterceptor(handler));
    }

    /**
     * 获取QueryExecutorImpl sendQuery方法无参拦截声明器
     *
     * @return InterceptDeclarer QueryExecutorImpl sendQuery方法无参拦截声明器
     */
    public static InterceptDeclarer getSendQueryInterceptDeclarer() {
        return InterceptDeclarer.build(getSendQueryMethodMatcher(), new QueryExecutorImplInterceptor());
    }

    /**
     * 获取QueryExecutorImpl类的ClassMatcher
     *
     * @return ClassMatcher 类匹配器
     */
    public static ClassMatcher getQueryExecutorImplClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS_NAME);
    }
}

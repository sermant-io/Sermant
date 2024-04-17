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

package com.huaweicloud.sermant.mariadbv3.utils;

import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;
import com.huaweicloud.sermant.database.handler.DatabaseHandler;
import com.huaweicloud.sermant.mariadbv3.interceptors.ExecutePipelineInterceptor;
import com.huaweicloud.sermant.mariadbv3.interceptors.SendQueryInterceptor;

/**
 * mariadb3.x declarer helper
 *
 * @author daizhenyu
 * @since 2024-01-30
 **/
public class MariadbV3EnhancementHelper {
    private static final String REPLAY_CLIENT_CLASS = "org.mariadb.jdbc.client.impl.ReplayClient";

    private static final String STANDARD_CLIENT_CLASS = "org.mariadb.jdbc.client.impl.StandardClient";

    private static final String SEND_QUERY_METHOD_NAME = "sendQuery";

    private static final String EXECUTE_PIPELINE_METHOD_NAME = "executePipeline";

    private MariadbV3EnhancementHelper() {
    }

    /**
     * Get ClassMatcher of ReplayClient
     *
     * @return ClassMatcher ClassMatcher
     */
    public static ClassMatcher getReplayClientClassMatcher() {
        return ClassMatcher.nameEquals(REPLAY_CLIENT_CLASS);
    }

    /**
     * Get ClassMatcher of StandardClient
     *
     * @return ClassMatcher ClassMatcher
     */
    public static ClassMatcher getStandardClientClassMatcher() {
        return ClassMatcher.nameEquals(STANDARD_CLIENT_CLASS);
    }

    /**
     * Get No-argument Interceptor of sendQuery Method
     *
     * @return InterceptDeclarer No-argument Interceptor of sendQuery Method
     */
    public static InterceptDeclarer getSendQueryInterceptDeclarer() {
        return InterceptDeclarer.build(getSendQueryMethodMatcher(),
                new SendQueryInterceptor());
    }

    /**
     * Get Parametric Interceptor of sendQuery Method
     *
     * @param handler write operation handler
     * @return InterceptDeclarer Parametric Interceptor of sendQuery Method
     */
    public static InterceptDeclarer getSendQueryInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getSendQueryMethodMatcher(),
                new SendQueryInterceptor(handler));
    }

    /**
     * Get No-argument Interceptor of executePipeline Method
     *
     * @return InterceptDeclarer No-argument Interceptor of executePipeline Method
     */
    public static InterceptDeclarer getExecutePipelineInterceptDeclarer() {
        return InterceptDeclarer.build(getExecutePipelineMethodMatcher(),
                new ExecutePipelineInterceptor());
    }

    /**
     * Get Parametric Interceptor of executePipeline Method
     *
     * @param handler write operation handler
     * @return InterceptDeclarer Parametric Interceptor of executePipeline Method
     */
    public static InterceptDeclarer getExecutePipelineInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getExecutePipelineMethodMatcher(),
                new ExecutePipelineInterceptor(handler));
    }

    private static MethodMatcher getSendQueryMethodMatcher() {
        return MethodMatcher.nameEquals(SEND_QUERY_METHOD_NAME);
    }

    private static MethodMatcher getExecutePipelineMethodMatcher() {
        return MethodMatcher.nameEquals(EXECUTE_PIPELINE_METHOD_NAME);
    }
}

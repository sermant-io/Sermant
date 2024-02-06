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
import com.huaweicloud.sermant.mariadbv3.interceptors.SendQueryInterceptor;

/**
 * mariadb3.x拦截点辅助类
 *
 * @author daizhenyu
 * @since 2024-01-30
 **/
public class MariadbV3EnhancementHelper {
    private static final String REPLAY_CLIENT_CLASS = "org.mariadb.jdbc.client.impl.ReplayClient";

    private static final String STANDARD_CLIENT_CLASS = "org.mariadb.jdbc.client.impl.StandardClient";

    private static final String SEND_QUERY_METHOD_NAME = "sendQuery";

    private MariadbV3EnhancementHelper() {
    }

    /**
     * 获取ReplayClient类的ClassMatcher
     *
     * @return ClassMatcher 类匹配器
     */
    public static ClassMatcher getReplayClientClassMatcher() {
        return ClassMatcher.nameEquals(REPLAY_CLIENT_CLASS);
    }

    /**
     * 获取StandardClient类的ClassMatcher
     *
     * @return ClassMatcher 类匹配器
     */
    public static ClassMatcher getStandardClientClassMatcher() {
        return ClassMatcher.nameEquals(STANDARD_CLIENT_CLASS);
    }

    /**
     * 获取sendQuery方法无参拦截器
     *
     * @return InterceptDeclarer sendQuery方法无参拦截器
     */
    public static InterceptDeclarer getSendQueryInterceptDeclarer() {
        return InterceptDeclarer.build(getSendQueryMethodMatcher(),
                new SendQueryInterceptor());
    }

    /**
     * 获取sendQuery方法有参拦截器
     *
     * @param handler 数据库自定义处理器
     * @return InterceptDeclarer sendQuery方法有参拦截器
     */
    public static InterceptDeclarer getSendQueryInterceptDeclarer(DatabaseHandler handler) {
        return InterceptDeclarer.build(getSendQueryMethodMatcher(),
                new SendQueryInterceptor(handler));
    }

    private static MethodMatcher getSendQueryMethodMatcher() {
        return MethodMatcher.nameEquals(SEND_QUERY_METHOD_NAME);
    }
}

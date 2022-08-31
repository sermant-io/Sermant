/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.grace.interceptors;

import com.huawei.registry.config.grace.GraceConstants;
import com.huawei.registry.config.grace.GraceContext;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import feign.Request;
import feign.Request.HttpMethod;
import feign.Response;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Feign请求测试
 *
 * @author zhouss
 * @since 2022-07-01
 */
public class SpringLoadbalancerFeignResponseInterceptorTest extends ResponseTest {
    /**
     * 测试请求流程
     *
     * @throws NoSuchMethodException 不会抛出
     */
    @Test
    public void testRequest() throws NoSuchMethodException {
        final Request request = Request
                .create(HttpMethod.GET, "http://provider:8888", Collections.emptyMap(), new byte[0],
                        StandardCharsets.UTF_8);
        final Response response = Response.builder()
                .headers(Collections.singletonMap(GraceConstants.MARK_SHUTDOWN_SERVICE_ENDPOINT,
                        Collections.singletonList(SHUTDOWN_ENDPOINT)))
                .status(HttpStatus.SC_OK)
                .request(request).build();
        Object[] arguments = new Object[]{request};
        final SpringLoadbalancerFeignResponseInterceptor interceptor = new SpringLoadbalancerFeignResponseInterceptor();
        final ExecuteContext executeContext = ExecuteContext.forMemberMethod(this,
                this.getClass().getDeclaredMethod("testRequest"), arguments, null, null);
        interceptor.before(executeContext);
        executeContext.changeResult(response);
        interceptor.after(executeContext);
        Assert.assertTrue(((Request) arguments[0]).headers().size() > 0);
        Assert.assertTrue(GraceContext.INSTANCE.getGraceShutDownManager().isMarkedOffline(SHUTDOWN_ENDPOINT));
    }
}

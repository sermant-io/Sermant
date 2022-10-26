/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.intergration.registry;

import com.huaweicloud.intergration.common.utils.EnvUtils;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * RestTemplate测试
 *
 * @author zhouss
 * @since 2022-10-26
 */
public class OkClientBootRegistryTest extends BootRegistryTest {
    @Rule(order = 204)
    public final BootRegistryRule bootRegistryRule = new BootRegistryRule();

    /**
     * 测试rest
     */
    @Test
    public void testOkClient() {
        if (canTestSync()) {
            check("okHttpClientGet", HttpMethod.GET);
        }
        if (canTestAsync()) {
            check("okHttpClientAsyncPost", HttpMethod.GET);
        }
    }

    @Override
    protected String getUrl() {
        return "http://localhost:8005/bootRegistry";
    }

    private boolean canTestSync() {
        return !"1.5.x".equals(EnvUtils.getEnv("app.version", null));
    }

    /**
     * 在springboot版本为2.7.2时, okhttp的异步场景方法getResponseWithInterceptorChain修饰改为internal, 拦截将失效
     *
     * @return 是否可执行异步
     */
    private boolean canTestAsync() {
        final String env = EnvUtils.getEnv("spring.boot.version", null);
        if (env == null) {
            return true;
        }
        final String[] parts = env.split("\\.");
        if (parts.length != 3) {
            return true;
        }
        int majorVersion = Integer.parseInt(parts[0]);
        if (majorVersion > 2) {
            return false;
        }
        int minVersion = Integer.parseInt(parts[1]);
        return minVersion < 7;
    }
}

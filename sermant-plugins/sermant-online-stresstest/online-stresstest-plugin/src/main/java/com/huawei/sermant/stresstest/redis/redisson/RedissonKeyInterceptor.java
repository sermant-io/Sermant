/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.stresstest.redis.redisson;

import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.stresstest.config.ConfigFactory;
import com.huawei.sermant.stresstest.core.Reflection;
import com.huawei.sermant.stresstest.core.Tester;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Redisson key 拦截器
 *
 * @author yiwei
 * @since 2021-11-02
 */
@SuppressWarnings({"checkstyle:MagicNumber"})
public class RedissonKeyInterceptor implements InstanceMethodInterceptor {
    private static final int COMMAND_LOCATION = 3;
    private static final String EVAL_COMMAND = "EVAL";
    private static final Pattern PATTERN = Pattern.compile("\\{(.+?)}");
    private static final String KEY = ConfigFactory.getConfig().getTestRedisPrefix();

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        if (Tester.isTest() && !ConfigFactory.getConfig().isRedisShadowRepositories()
            && arguments.length > COMMAND_LOCATION + 1) {
            modifyRedissonCommand(arguments[COMMAND_LOCATION], (Object[])arguments[COMMAND_LOCATION + 1]);
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
    }

    private void modifyRedissonCommand(Object command, Object[] params) {
        Reflection.getDeclaredValue("name", command).ifPresent(name -> {
            // EVAL 命令，第一个参数是带参数的命令行，第二个参数是参数个数，后面是所有的参数
            if (name.toString().equalsIgnoreCase(EVAL_COMMAND)) {
                if (params.length < 3) {
                    return;
                }
                if (params[1] instanceof Integer) {
                    for (int i = 0; i < (Integer)params[1]; i++) {
                        modifyKey(params, i + 2);
                    }
                }
                return;
            }
            for (int i = 0; i < params.length; i++) {
                if (!modifyKey(params, i)) {
                    break;
                }
            }
        });
    }

    private boolean modifyMainKey(Object[] params, int index) {
        String content = (String)params[index];
        Matcher matcher = PATTERN.matcher(content);
        if (matcher.find()) {
            String original = matcher.group(1);
            if (!original.startsWith(KEY)) {
                params[index] = content.replaceAll(original, KEY + original);
                return true;
            }
        }
        return false;
    }

    private boolean modifyKey(Object[] params, int index) {
        if (!(params[index] instanceof String)) {
            return false;
        }
        if (modifyMainKey(params, index)) {
            return true;
        }
        String content = (String)params[index];
        if (!content.startsWith(KEY)) {
            params[index] = KEY + params[index];
            return true;
        }
        return false;
    }
}

/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */
package com.lubanops.stresstest.redis.redisson;

import com.huawei.javamesh.core.agent.common.BeforeResult;
import com.huawei.javamesh.core.agent.interceptor.InstanceMethodInterceptor;
import com.lubanops.stresstest.config.ConfigFactory;
import com.lubanops.stresstest.core.Reflection;
import com.lubanops.stresstest.core.Tester;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Redisson key 拦截器
 *
 * @author yiwei
 * @since 2021/11/2
 */
public class RedissonKeyInterceptor implements InstanceMethodInterceptor {
    private static final int COMMAND_LOCATION = 3;
    private static final String EVAL_COMMAND = "EVAL";
    private static final Pattern PATTERN = Pattern.compile("\\{(.+?)}");
    private static final String KEY = ConfigFactory.getConfig().getTestRedisPrefix();

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        if (Tester.isTest() && !ConfigFactory.getConfig().isRedisShadowRepositories() && arguments.length > COMMAND_LOCATION + 1) {
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
        String content = (String) params[index];
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
        String content = (String) params[index];
        if (!content.startsWith(KEY)) {
            params[index] = KEY + params[index];
            return true;
        }
        return false;
    }
}

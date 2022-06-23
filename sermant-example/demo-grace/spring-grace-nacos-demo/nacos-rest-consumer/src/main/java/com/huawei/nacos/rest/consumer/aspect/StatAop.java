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

package com.huawei.nacos.rest.consumer.aspect;

import com.huawei.nacos.common.CommonConstants;
import com.huawei.nacos.rest.consumer.entity.MyHashMap;
import com.huawei.nacos.rest.consumer.entity.ResponseInfo;
import com.huawei.nacos.rest.consumer.enums.GraceStatEnum;
import com.huawei.nacos.rest.consumer.stat.RequestStat;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

/**
 * 请求统计
 *
 * @author zhouss
 * @since 2022-06-17
 */
@Aspect
@Component
public class StatAop {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatAop.class);

    private final Map<String, RequestStat> statMap = new MyHashMap<>();

    /**
     * 优雅上下线切面
     */
    @Pointcut("execution (* com.huawei.nacos.rest.consumer.GraceController.*(..))")
    public void grace() {
    }

    /**
     * 环绕方法
     *
     * @param joinPoint 切点
     * @throws Throwable 请求异常时抛出
     * @return 请求结果
     */
    @Around("grace()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object obj = null;
        final String name = joinPoint.getSignature().getName();
        Throwable throwable = null;
        try {
            obj = joinPoint.proceed();
        } catch (Throwable ex) {
            throwable = ex;
        } finally {
            final Optional<GraceStatEnum> graceStatEnum = GraceStatEnum.match(name);
            if (!graceStatEnum.isPresent()) {
                LOGGER.warn("Can not match url [{}] for stat!", name);
            } else {
                final RequestStat stat = statMap.getOrDefault(name, graceStatEnum.get().getRequestStat());
                final Optional<ResponseInfo> responseInfo = buildResponseInfo(joinPoint.getArgs());
                if (responseInfo.isPresent()) {
                    stat.stat(throwable, responseInfo.get());
                }
                statMap.put(name, stat);
            }
            if (throwable != null) {
                throw throwable;
            }
        }
        return obj;
    }

    private Optional<ResponseInfo> buildResponseInfo(Object[] args) {
        final Optional<HttpServletResponse> response1 = getResponse(args);
        if (!response1.isPresent()) {
            return Optional.empty();
        }
        final HttpServletResponse response = response1.get();
        return Optional.of(new ResponseInfo(response.getHeader(CommonConstants.PORT_KEY),
                response.getHeader(CommonConstants.IP_KEY),
                response.getHeader(CommonConstants.SERVICE_NAME_KEY),
                Boolean.parseBoolean(response.getHeader(CommonConstants.WARM_UP_STATE)),
                response.getHeader(CommonConstants.QPS_KEY) == null ? -1
                        : Integer.parseInt(response.getHeader(CommonConstants.QPS_KEY))));
    }

    private Optional<HttpServletResponse> getResponse(Object[] args) {
        if (args == null || args.length == 0) {
            return Optional.empty();
        }
        for (Object arg : args) {
            if (arg instanceof HttpServletResponse) {
                return Optional.of((HttpServletResponse) arg);
            }
        }
        return Optional.empty();
    }

    public Map<String, RequestStat> getStatMap() {
        return statMap;
    }
}

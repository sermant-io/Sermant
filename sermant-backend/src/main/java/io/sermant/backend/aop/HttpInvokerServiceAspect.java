/*
 *  Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.backend.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * CVE-2016-1000027 Vulnerability resolved
 *
 * @author daizhenyu
 * @since 2023-12-27
 **/
@Component
@Aspect
public class HttpInvokerServiceAspect {
    /**
     * Set AOP pointcut
     */
    @Pointcut("execution(* org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter.handleRequest(..))")
    public void handleRequestPointcut() {
    }

    /**
     * Execute method around
     *
     * @param point
     * @return object
     */
    @Around(value = "handleRequestPointcut()")
    public Object aroundHandleRequest(ProceedingJoinPoint point) {
        return null;
    }
}

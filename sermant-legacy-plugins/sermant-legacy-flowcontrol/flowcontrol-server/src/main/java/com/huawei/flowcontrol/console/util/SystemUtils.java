/*
 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.console.util;

import com.huawei.flowcontrol.console.auth.AuthServiceImpl;
import com.huawei.flowcontrol.console.auth.AuthUserImpl;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 系统工具类，获取HttpServletRequest中的信息
 *
 * @author XiaoLong Wang
 * @since 2020-12-21
 */
@Component
public class SystemUtils {
    /**
     * 失败ERROR_CODE
     */
    public static final int ERROR_CODE = -1;

    /**
     * long型初始值
     */
    public static final long LONG_INITIALIZE = 0L;

    /**
     * 是否加载cas bean
     */
    private static boolean isMoLoad;

    @Value(value = "${conditional.cas.load}")
    public void setMoLoad(boolean isManageOneLoad) {
        isMoLoad = isManageOneLoad;
    }

    /**
     * 获取用户名
     *
     * @param httpServletRequest 请求对象
     * @return 返回用户名
     */
    public static String getUserName(HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession(false);
        String username = null;
        if (session != null) {
            if (isMoLoad) {
                Object moLoginInformation = session.getAttribute(DataType.CONST_CAS_ASSERTION.getDataType());
                Assertion assertion = moLoginInformation instanceof Assertion
                    ? (Assertion) moLoginInformation : null;
                if (assertion != null) {
                    username = assertion.getPrincipal().getName();
                }
            } else {
                Object consoleLoginInformation = session.getAttribute(AuthServiceImpl.WEB_SESSION_KEY);
                AuthUserImpl authUser = consoleLoginInformation instanceof AuthUserImpl
                    ? (AuthUserImpl) consoleLoginInformation : null;
                if (authUser != null) {
                    username = authUser.getUsername();
                }
            }
        }
        return sanitizeUser(username);
    }

    /**
     * 打印日志信息处理
     *
     * @param message 需要打印信息
     * @return 处理完后信息
     */
    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    public static String sanitizeUser(String message) {
        if (message == null) {
            return null;
        }
        return message.replace("\r", "\\r").replace("\n", "\\n");
    }
}

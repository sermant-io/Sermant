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

package com.huawei.flowcontrol.console.controller;

import com.huawei.flowcontrol.console.entity.Result;
import com.huawei.flowcontrol.console.util.DataType;
import com.huawei.flowcontrol.console.util.SystemUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 文件名：LoginController 版权： 描述：处理登录请求
 *
 * @author Gaofang Wu
 * @since 2020-11-25 跟踪单号： 修改单号： 修改内容：添加获取全息排查url的接口
 */
@RestController
@RequestMapping("/auth")
public class LoginController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    private static final Logger OPERATE_LOGGER = LoggerFactory.getLogger("OPERATE_LOGGER");

    /**
     * 注销url
     */
    @Value("${casClientLogoutUrl}")
    private String logoutUrl;

    /**
     * cas服务端的登录地址
     */
    @Value("${cas.server-login-url}")
    private String casLoginUrl;

    /**
     * 当前服务器的地址(客户端)
     */
    @Value("${cas.client-host-url}")
    private String casClientUrl;

    /**
     * 是否加载cas bean
     */
    @Value("${conditional.cas.load}")
    private boolean isMoLoad;

    /**
     * 全息排查url
     */
    @Value("${holographic.investigation.url}")
    private String hiUrl;

    private String loginUrl;

    @PostConstruct
    public void init() {
        String serviceUrl = casClientUrl.replace(":", "%3A");
        serviceUrl = serviceUrl.replace("/", "%2F");
        if (isMoLoad) {
            loginUrl = casLoginUrl + "?service=" + serviceUrl + "auth%2FtoLogin";
        }
    }

    /**
     * 根据登录方式返回不同的登录url
     *
     * @return 返回登录路径
     */
    @RequestMapping(value = "/loginUrl", method = RequestMethod.GET)
    public Result<String> loginUrl() {
        if (isMoLoad) {
            return Result.ofSuccess(loginUrl);
        }
        return Result.ofSuccess(null);
    }

    /**
     * 重定向到前端
     *
     * @param httpServletRequest request
     * @param response response
     */
    @RequestMapping(value = "/toLogin", method = RequestMethod.GET)
    public void toLogin(HttpServletRequest httpServletRequest, HttpServletResponse response) {
        try {
            if (isMoLoad) {
                String username = SystemUtils.getUserName(httpServletRequest);
                OPERATE_LOGGER.info("{} login the system,{}!", username, DataType.OPERATION_SUCCESS.getDataType());
            }

            if (verifyUrl(casClientUrl)) {
                // 重定向到前端
                response.sendRedirect(casClientUrl + "?loginSuccess");
                LOGGER.info("ToLogin success");
            } else {
                throw new UnknownHostException();
            }
        } catch (IOException e) {
            LOGGER.error("Redirect fail,toLogin fail");
        }
    }

    /**
     * 验证是否是URL
     *
     * @param url
     * @return 是否为url
     */
    public static boolean verifyUrl(String url) {
        // URL验证规则
        String regEx = "[a-zA-z]+://[^\\s]*";

        // 编译正则表达式
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    /**
     * 处理注销请求
     *
     * @param request
     * @return 是否成功
     */
    @RequestMapping(value = "/logout")
    public Result<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String userName = SystemUtils.getUserName(request);
        OPERATE_LOGGER.info("{} logout the system!", userName);
        if (session != null) {
            session.invalidate();
            OPERATE_LOGGER.info("{} logout the system,{}!", userName, DataType.OPERATION_SUCCESS.getDataType());
            LOGGER.info("Logout success");
            if (isMoLoad) {
                return Result.ofSuccess(logoutUrl + "auth%2FtoLogin");
            } else {
                return Result.ofSuccess("");
            }
        }
        return Result.ofSuccess("");
    }

    /**
     * 处理首页请求：判断用户是否存在
     *
     * @return 返回成功
     */
    @RequestMapping(value = "/check")
    public Result<String> check() {
        LOGGER.info("check success");
        return Result.ofSuccess("success");
    }

    /**
     * 获取全息排查url
     *
     * @return 全息排查地址
     */
    @RequestMapping(value = "/getHiUrl")
    public Result<String> getHiUrl() {
        LOGGER.info("Get holographic.investigation.url");
        return Result.ofSuccess(hiUrl);
    }
}

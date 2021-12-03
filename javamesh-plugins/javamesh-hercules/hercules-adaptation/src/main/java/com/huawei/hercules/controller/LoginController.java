/*
 * Copyright (C) Huawei Technologies Co., Ltd. $YEAR$-$YEAR$. All rights reserved
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

package com.huawei.hercules.controller;

import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.service.ILoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private ILoginService loginService;

    @RequestMapping(value = "/user/login", method = RequestMethod.POST)
    public JSONObject login(HttpServletResponse response, @RequestBody JSONObject params) {
        String username = params.getString("username");
        String password = params.getString("password");
        String nativeLanguage = "cn";
        String userTimezone = "Asia/Shanghai";
        JSONObject jsonObject = loginService.login(username, password, nativeLanguage, userTimezone);
        if (jsonObject != null && Boolean.parseBoolean(jsonObject.get("success").toString())) {
            String sessionId = jsonObject.get("JSESSIONID").toString();
            Cookie cookie = new Cookie("JSESSIONID", sessionId);
            cookie.setMaxAge(12 * 60 * 60);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }
        return jsonObject;
    }

    @RequestMapping("/user/logout")
    public String logout(HttpServletResponse response) {
        String logout = loginService.logout();
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return logout;
    }

    @RequestMapping("/status")
    public HttpEntity<String> status() {
        return loginService.status("");
    }

}

/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.hercules.service;

import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.config.FeignRequestInterceptor;
import com.huawei.hercules.fallback.LoginServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        url = "${controller.engine.url}",
        name = "login",
        fallbackFactory = LoginServiceFallbackFactory.class,
        configuration = FeignRequestInterceptor.class
)
public interface ILoginService {
    @RequestMapping(value = "/form_login", method = RequestMethod.POST)
    JSONObject login(@RequestParam("j_username") String username,
                     @RequestParam("j_password") String password,
                     @RequestParam("native_language") String nativeLanguage,
                     @RequestParam("user_timezone") String userTimezone);

    @RequestMapping("/logout")
    String logout();

    @RequestMapping(value = "/rest/perftest/api/status", method = RequestMethod.GET)
    HttpEntity<String> status(@RequestParam("ids") String ids);

}
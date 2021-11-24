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
package com.huawei.common.util;

import com.alibaba.fastjson.JSONObject;
import com.huawei.common.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(url = "${decisionEngine.url}", name = "login",configuration = FeignConfiguration.class)
public interface UserFeignClient {
    @RequestMapping(value = "/form_login", method = RequestMethod.POST)
    JSONObject login(@RequestParam("j_username") String username,
                     @RequestParam("j_password") String password,
                     @RequestParam("native_language") String nativeLanguage,
                     @RequestParam("user_timezone") String userTimezone);

    @RequestMapping(value = "/user/api/information",method = RequestMethod.GET)
    JSONObject getUserInfo();

    @RequestMapping(value = "/user/api/password",method = RequestMethod.POST)
    String encodePassword(JSONObject object);

    @RequestMapping("/logout")
    String logout();
}

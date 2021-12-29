package com.huawei.common.util;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component
public class RestTemplateUtil {
    @Autowired
    private RestTemplate restTemplate;

    private static RestTemplate client;

    @PostConstruct
    public void init(){
        client = restTemplate;
    }

    public static ResponseEntity sendPostRequest(HttpServletRequest request, String url, Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie",request.getHeader("Cookie"));
        HttpEntity httpEntity = new HttpEntity(params, headers);
        return client.exchange(url, HttpMethod.POST, httpEntity, JSONObject.class);
    }
}

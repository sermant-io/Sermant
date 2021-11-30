package com.huawei.hercules.service;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Configuration
public class MyRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    Enumeration<String> headers = request.getHeaders(name);
                    while (headers.hasMoreElements()) {
                        String value = headers.nextElement();
                        template.header(name, value);
                        if ("cookie".equalsIgnoreCase(name)) {
                            template.header(name, "ngrinder_lang=cn");
                        }
                    }
                }
            }
        }
    }
}

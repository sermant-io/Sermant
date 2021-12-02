package com.huawei.hercules.exception;

import com.alibaba.fastjson.JSONObject;
import feign.FeignException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class HerculesExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public JSONObject exceptionHandler(Exception e) {
        // build errorMsg
        JSONObject errorMsg = new JSONObject();
        errorMsg.put("code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        errorMsg.put("msg", e.getMessage());

        // check weather login
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        if (servletRequestAttributes == null) {
            return errorMsg;
        }
        HttpServletResponse response = servletRequestAttributes.getResponse();
        if (response == null) {
            return errorMsg;
        }
        if (!(e instanceof FeignException.Unauthorized)) {
            return errorMsg;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        errorMsg.put("code", HttpServletResponse.SC_UNAUTHORIZED);
        errorMsg.put("msg", "UNAUTHORIZED");
        return errorMsg;
    }
}

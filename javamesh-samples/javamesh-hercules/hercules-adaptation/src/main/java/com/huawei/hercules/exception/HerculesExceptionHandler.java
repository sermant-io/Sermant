package com.huawei.hercules.exception;

import com.alibaba.fastjson.JSONObject;
import feign.FeignException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class HerculesExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public JSONObject exceptionHandler(Exception e) {
        // 返回401
        JSONObject errorMsg = new JSONObject();
        if (e instanceof FeignException.Unauthorized) {
            errorMsg.put("code", HttpServletResponse.SC_UNAUTHORIZED);
            errorMsg.put("message", "UNAUTHORIZED");
        } else {
            errorMsg.put("code", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            errorMsg.put("message", e.getMessage());
        }
        return errorMsg;
    }
}

package com.huawei.user.common.filter;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.huawei.user.common.api.CommonResult;
import com.huawei.user.common.util.UserFeignClient;
import com.huawei.user.entity.UserEntity;
import com.huawei.user.mapper.UserMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebFilter(urlPatterns = {"/*"})
@Slf4j
public class UserFilter implements Filter {
    @Resource
    private UserFeignClient userFeignClient;

    @Autowired
    private UserMapper mapper;

    private HttpSession session;

    private UserEntity user;

    private static final Set<String> ALLOWED_PATHS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("/api/user/login", "/api/user/registe", "/api/user/me","/api/user/logout")));

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getRequestURI().substring(request.getContextPath().length()).replaceAll("[/]+$", "");
        if (!ALLOWED_PATHS.contains(path)) {
            try {
                JSONObject userInfo = userFeignClient.getUserInfo();
                session = request.getSession();
                String userId = (String) userInfo.get("userId");
                String enabled = mapper.getUserStatus(userId);
                if (StringUtils.isNotBlank(enabled) && enabled.equals("F")) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("msg", "账户已被禁用");
                    responseJson(response, jsonObject);
                    return;
                }
                String role = mapper.getRoleByUserName(userId);
                List<String> auth = mapper.getAuthByRole(role);
                user = new UserEntity(userId, (String) userInfo.get("userName"), role, auth);
                session.setAttribute("userInfo", user);
            } catch (FeignException e) {
                log.error("No login. ");
                response.setStatus(401);
                return;
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void responseJson(HttpServletResponse response, Object obj) {
        response.setContentType("application/json; charset=utf-8");
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.print(JSONObject.toJSONString(obj, SerializerFeature.WriteMapNullValue,
                    SerializerFeature.WriteDateUseDateFormat));
            response.flushBuffer();
        } catch (IOException e) {
            log.error("Exception occurs. Exception info {}", e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}

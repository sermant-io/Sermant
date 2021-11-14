package org.ngrinder.security;

import com.alibaba.fastjson.JSONObject;
import org.ngrinder.common.constant.WebConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * add
 */
public class NGrinderAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	@Override
	public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
		String sessionId = httpServletRequest.getSession().getId();
		httpServletResponse.setContentType("application/json; charset=UTF-8");
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("JSESSIONID", sessionId);
		jsonObject.put(WebConstants.JSON_SUCCESS, true);
		jsonObject.put("user_timezone", httpServletRequest.getParameter("user_timezone"));
		jsonObject.put("native_language", httpServletRequest.getParameter("native_language"));
		httpServletResponse.getWriter().write(jsonObject.toJSONString());
		httpServletResponse.flushBuffer();
	}
}

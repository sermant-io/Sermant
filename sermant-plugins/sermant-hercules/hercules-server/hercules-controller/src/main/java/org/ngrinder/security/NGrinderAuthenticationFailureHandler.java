package org.ngrinder.security;

import com.alibaba.fastjson.JSONObject;
import org.ngrinder.common.constant.WebConstants;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * add
 */
public class NGrinderAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
		response.setContentType("application/json; charset=UTF-8");
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(WebConstants.JSON_SUCCESS, false);
		response.getWriter().write(jsonObject.toJSONString());
		response.flushBuffer();
	}
}

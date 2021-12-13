package org.ngrinder.security;

import org.apache.commons.httpclient.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @Author: j00466872
 * @Date: 2019/4/18 20:27
 */
@Component("webFliter")
//@ServletComponentScan
@WebFilter(urlPatterns = "/*", filterName = "RequestCollecter")
public class WebFliter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
		throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		//跨域请求，*代表允许全部类型
		String s = request.getHeader("Origin");
		response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		//允许请求方式
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE,PUT");
		//用来指定本次预检请求的有效期，单位为秒，在此期间不用发出另一条预检请求
		response.setHeader("Access-Control-Max-Age", "3600");
		//请求包含的字段内容，如有多个可用哪个逗号分隔如下
		response.setHeader("Access-Control-Allow-Headers", "content-type,x-requested-with,Authorization, x-ui-request,lang");
		//访问控制允许凭据，true为允许
		response.setHeader("Access-Control-Allow-Credentials", "true");
		// 浏览器是会先发一次options请求，如果请求通过，则继续发送正式的post请求
		// 配置options的请求返回
		if (request.getMethod().equals("OPTIONS")) {
			response.setStatus(HttpStatus.SC_OK);
			response.getWriter().write("OPTIONS returns OK");
			return;
		}
		// 传递业务请求处理
		chain.doFilter(servletRequest, servletResponse);
	}

	@Override
	public void destroy() {

	}

}

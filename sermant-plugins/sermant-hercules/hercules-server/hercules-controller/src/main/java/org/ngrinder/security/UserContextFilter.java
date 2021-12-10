package org.ngrinder.security;


import org.apache.commons.httpclient.HttpStatus;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Controller
@Component("userContextFilter")
//@ServletComponentScan
@WebFilter(urlPatterns = "/*", filterName = "userContextFilter")
public class UserContextFilter implements Filter{


	private UserService userService;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
		userService = webApplicationContext.getBean(UserService.class);
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
		throws IOException, ServletException {


		String username = (String)servletRequest.getAttribute("ArgusJWTAuthenticationFilter.USERNAME_ATTRIBUTE_KEY");
		if (username != null){
			User user = userService.getOne(username);
			if(user == null){
				user = userService.createUser(new User(username, username, null, null, null));
			}
			user.setTimeZone("Asia/Shanghai");
			user.setUserLanguage("en");
			final SecuredUser finalUser = new SecuredUser(user, null);
			SecurityContextHolder.getContext().setAuthentication(new Authentication() {
				@Override
				public Collection<? extends GrantedAuthority> getAuthorities() {
					return null;
				}

				@Override
				public Object getCredentials() {
					return null;
				}

				@Override
				public Object getDetails() {
					return finalUser;
				}

				@Override
				public Object getPrincipal() {
					return finalUser;
				}

				@Override
				public boolean isAuthenticated() {
					return true;
				}

				@Override
				public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

				}

				@Override
				public String getName() {
					return finalUser.getUser().getUserName();
				}
			});
		}

		// 传递业务请求处理
		chain.doFilter(servletRequest, servletResponse);
	}

	@Override
	public void destroy() {

	}

}

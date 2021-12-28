package org.ngrinder.security;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class NoAuthHandlerExceptionResolver implements HandlerExceptionResolver, Ordered {

	protected static final Log logger = LogFactory.getLog(NoAuthHandlerExceptionResolver.class);

	@Override
	public int getOrder() {
		return -2;
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		if (ex instanceof AuthenticationException){
			try {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return new ModelAndView();
			} catch (Exception handlerException) {
				handlerException.printStackTrace();
				logger.warn("Handling of [" + ex.getClass().getName() + "] resulted in Exception", handlerException);
			}
		}
		return null;
	}
}

package com.huawei.argus.restcontroller;

import org.ngrinder.common.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/dev")
public class RestDevController extends RestBaseController {

	@Autowired
	private ReloadableResourceBundleMessageSource messageSource;

	/**
	 * Refresh the messages.
	 *
	 * @return "redirect:/"
	 */
	@RequestMapping("/msg")
	public HttpEntity<String> refreshMessage() {
		messageSource.clearCacheIncludingAncestors();
		return successJsonHttpEntity();
	}

}


package com.huawei.argus.restcontroller;

import com.alibaba.fastjson.JSONObject;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.user.controller.UserController;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.ngrinder.common.util.Preconditions.checkTrue;

@RestController
@RequestMapping("/rest/sign_up")
public class RestUserSignUpController extends RestUserController {

	@Autowired
	private UserService userService;

	@Autowired
	private Config config;

	/**
	 * New user sign up form login page.[url重新命名]
	 *
	 * @return "user/sign_up_modal"
	 */
	@RequestMapping("/newUser")
	public JSONObject openForm() {
		checkTrue(config.isSignUpEnabled(), "Access to this url is not allowed when sign up is disabled");
		JSONObject modelInfos = super.openForm(null);
		modelInfos.put("allowShareChange", false);
		modelInfos.put("showPasswordByDefault", true);
		modelInfos.put("newUser", true);
		return modelInfos;
	}


	/**
	 * To block security issue.[未改变，不使用]
	 *
	 * @deprecated
	 */
	@RequestMapping("/new_remap")
	public String openForm(User user, ModelMap model) {
		return null;
	}

	/**
	 * Get user list that current user will be shared, excluding current user.
	 *
	 * @param user  current user
	 * @param model model
	 */
	protected void attachCommonAttribute(User user, ModelMap model) {
		model.addAttribute("userSecurityEnabled", config.isUserSecurityEnabled());
	}

	/**
	 * Save a user.[url重新命名]
	 *
	 * @param newUser user to be added.
	 * @return "redirect:/"
	 */
	@RequestMapping("/saveUser")
	public String saveUser(@ModelAttribute("user") User newUser) {
		checkTrue(config.isSignUpEnabled(), "Access to this url is not allowed when sign up is disabled");
		newUser.setRole(Role.USER);
		userService.createUser(newUser);
		return returnSuccess();
	}

	/**
	 * To block security issue.
	 *
	 * @param user        current user
	 * @param updatedUser user to be updated.
	 * @param model       model
	 * @return
	 * @deprecated
	 */
	@RequestMapping("/save_remap")
	public String save(User user, @ModelAttribute("user") User updatedUser, ModelMap model) {
		return null;
	}

	/**
	 * To block security issue.
	 *
	 * @param userId userId to be checked
	 * @return
	 * @deprecated
	 */
	@RestAPI
	@RequestMapping("/api/{userId}/check_duplication_remap")
	public HttpEntity<String> checkDuplication(@PathVariable String userId) {
		return null;
	}

	/**
	 * Check the user id existence.
	 *
	 * @param userId userId to be checked
	 * @return success json if true.
	 */
	@RestAPI
	@RequestMapping("/api/{userId}/check_duplication")
	public HttpEntity<String> checkDuplicationForRegistration(@PathVariable String userId) {
		checkTrue(config.isSignUpEnabled(), "Access to this url is not allowed when sign up is disabled");
		User user = userService.getOne(userId);
		return (user == null) ? successJsonHttpEntity() : errorJsonHttpEntity();
	}
}


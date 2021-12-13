/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.argus.user;

import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.model.User;
import org.ngrinder.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;

import static org.ngrinder.common.util.Preconditions.checkNull;

/**
 * 功能描述：
 *
 *
 * @since 2021-11-08
 */
@Controller
@RequestMapping("/user")
public class UserExtendController extends BaseController {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(UserExtendController.class);

	@Autowired
	private UserService userService;

	/**
	 * encode a password.
	 *
	 * @param user new user
	 * @return json message
	 */
	@RestAPI
	@RequestMapping(value = {"/api/password"}, method = RequestMethod.POST)
	public HttpEntity<String> password(@RequestBody User user) {
		LOGGER.debug("Start to encode password for user[{}]", user.getUserId());
		checkNull(user.getId(), "User DB ID should be null");
		userService.encodePassword(user);
		return toJsonHttpEntity(user.getPassword());
	}

	/**
	 * get user info.
	 *
	 * @param user request information
	 * @return json message
	 */
	@RestAPI
	@RequestMapping(value = {"/api/information"}, method = RequestMethod.GET)
	public HttpEntity<String> getInformation(User user) {
		if (user == null || StringUtils.isEmpty(user.getUserId())) {
			//LOGGER.error("No login, get user information fail.");
			String returnError = returnError("No login, get user information fail.");
			return toJsonHttpEntity(returnError);
		}
		LOGGER.debug("Start to search information for user[{}]", user.getUserId());
		User userInfo = userService.getOne(user.getUserId());
		return toJsonHttpEntity(userInfo);
	}
}

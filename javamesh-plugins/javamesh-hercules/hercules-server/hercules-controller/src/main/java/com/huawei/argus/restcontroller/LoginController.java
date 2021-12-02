/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.argus.restcontroller;

import com.alibaba.fastjson.JSONObject;
import org.ngrinder.common.constant.WebConstants;
import org.ngrinder.common.controller.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录结果返回
 *
 * @author q30008110
 * @since 3.0
 */
@RestController
public class LoginController extends RestBaseController {

	public static final String LOGIN_SUCCESS = "Success";
	public static final String LOGIN_FAILURE = "Failure";

	@RequestMapping("/loginSuccess")
	public String loginSuccess(HttpServletRequest request,HttpServletResponse response) {
		String sessionId = request.getSession().getId();
		System.out.println("sessionId:" + sessionId);
		String requestedSessionId = request.getRequestedSessionId();
		System.out.println("requestedSessionId:" + requestedSessionId);

		return sessionId;
	}

	@RequestMapping("/loginFailure")
	public JSONObject loginFailure(HttpServletRequest request, HttpServletResponse response) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(WebConstants.JSON_SUCCESS, false);
		return jsonObject;
	}

	@RequestMapping("/logoutSuccess")
	public String logoutSuccess() {
		return returnSuccess();
	}

}

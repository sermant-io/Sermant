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

package com.huawei.argus.script.controller;

import io.swagger.annotations.Api;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.model.User;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.script.service.ScriptValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Author j00466872
 * @Date 2019/4/29 20:04
 */
@Api
@RestController
@RequestMapping("/perf_scripts")
public class ScriptController extends BaseController {

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private ScriptValidationService scriptValidationService;

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;

	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public Object list(User user){
		return fileEntryService.getAll(user);
	}

	@RequestMapping(value = "traffic", method = RequestMethod.POST)
	public Object createArgusTrafficSceneScript(User user, @RequestBody Map<String, Object> requestBody) {
		fileEntryService.prepareNewEntryForFlowTest(
			user,
			requestBody.get("sceneJson").toString(),
			requestBody.get("path").toString(),
			requestBody.get("sceneName").toString(),
			null,
			scriptHandlerFactory.getHandler("argus_traffic").getClass());
		return true;
	}
}

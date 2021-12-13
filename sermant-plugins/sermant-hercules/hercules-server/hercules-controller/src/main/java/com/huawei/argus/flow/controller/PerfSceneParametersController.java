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

package com.huawei.argus.flow.controller;

import com.huawei.argus.flow.service.PerfSceneParametersService;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.model.PerfScene;
import org.ngrinder.model.PerfSceneParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/perftest/scene-parameters")
public class PerfSceneParametersController extends BaseController {

	@Autowired
	PerfSceneParametersService perfSceneParametersService;

	@RequestMapping(value = {"/"},method = RequestMethod.POST)
	@ResponseBody
	public PerfSceneParameters createScene(@RequestBody PerfSceneParameters perfSceneParameters){
		System.out.println(perfSceneParameters);

		return perfSceneParametersService.create(perfSceneParameters);
	}
}

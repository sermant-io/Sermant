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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.google.gson.Gson;
import com.huawei.argus.common.JsonUtil;
import com.huawei.argus.flow.service.impl.PerfSceneAPiService;
import com.huawei.argus.scene.service.PerfSceneService;
import io.swagger.annotations.Api;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.model.PerfScene;
import org.ngrinder.model.User;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by x00377290 on 2019/4/19.
 */
@Controller
@RequestMapping("/perftest/perfSceneApi")
@Api
public class PerfSceneApiControler extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PerfSceneApiControler.class);

	@Autowired
	public PerfSceneAPiService perfSceneAPiService;

	@Autowired
	public PerfSceneService perfSceneService;

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;

	public static final String SCRIPT_TYPE = "argus_flow";

	/**
	 * 创建场景 包含API 列表及参数
	 * @param sceneDomain 场景实例
	 * @return
	 */
    @RequestMapping(value = {""},method = RequestMethod.POST)
    @ResponseBody
    public Object createScene(User user,@RequestBody PerfScene sceneDomain){
		ResponseEntity<Object> objectResponseEntity ;
		try {
			PerfScene sceneNew = perfSceneService.create(user,sceneDomain);
			if (sceneNew != null){
				return  sceneNew;
			}else {
				objectResponseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
				return objectResponseEntity;
			}
		}catch (Throwable throwable){
			LOGGER.error(throwable.getMessage());
			objectResponseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			throwable.printStackTrace();
		}
		return objectResponseEntity;
    }

	@RequestMapping(value = {"/script"},method = RequestMethod.POST)
    public Object createScript(User user, @RequestBody PerfScene sceneDomain){
		ResponseEntity<Object> objectResponseEntity = new ResponseEntity<>(HttpStatus.OK);;
    	try {
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writeValueAsString(sceneDomain);
			fileEntryService.prepareNewEntryForFlowTest(user,json,sceneDomain.getSceneName()+"_"+sceneDomain.getId() ,sceneDomain.getSceneName(),
				scriptHandlerFactory.getHandler(SCRIPT_TYPE),scriptHandlerFactory.getHandler(SCRIPT_TYPE).getClass());
		}catch (Throwable throwable){
			LOGGER.error(throwable.getMessage());
			objectResponseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			throwable.printStackTrace();
		}
		return objectResponseEntity;
	}
	/**
	 * 更新场景
	 * @param sceneDomain 场景实例
	 * @return
	 */
	@RequestMapping(value = {"/{id}"},method = RequestMethod.PUT, consumes = "application/json")
	@ResponseBody
    public Object updateScene(User user, @PathVariable long id, @RequestBody PerfScene sceneDomain){
		ResponseEntity<Object> responseEntity = null;
		try {
			PerfScene tempPerfScene = perfSceneService.retrieve(id);
			if (tempPerfScene == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}else {
				return perfSceneService.update(user, id, sceneDomain);
			}
		}catch (Throwable throwable){
			LOGGER.error(throwable.getMessage());
			responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			throwable.printStackTrace();
		}
		return responseEntity;
    }

}

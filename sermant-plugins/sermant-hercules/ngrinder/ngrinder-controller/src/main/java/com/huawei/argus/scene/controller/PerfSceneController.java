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

package com.huawei.argus.scene.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.argus.exceptions.CreateScriptException;
import com.huawei.argus.flow.controller.PerfSceneApiControler;
import com.huawei.argus.flow.service.impl.PerfSceneAPiService;
import com.huawei.argus.scene.service.PerfSceneService;
import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.ngrinder.model.PerfScene;
import org.ngrinder.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

import static org.ngrinder.common.util.ObjectUtils.defaultIfNull;

/**
 * @Author: j00466872
 * @Date: 2019/4/22 15:25
 */
@Api
@RestController
@RequestMapping("/perf_scenes")
public class PerfSceneController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PerfSceneApiControler.class);
	@Autowired
	private PerfSceneService perfSceneService;

	@Autowired
	private PerfSceneAPiService perfSceneAPiService;

	private ObjectMapper mapper = new ObjectMapper();

	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public Object list(User user, @PageableDefault(page = 0, size = 10) Pageable pageable) {
		pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(),
						defaultIfNull(pageable.getSort(), new Sort(Sort.Direction.DESC, "id")));
		return perfSceneService.listPaged(user, pageable);
	}

	@RequestMapping(value = {"/{id}", "/{id}/"}, method = RequestMethod.GET)
	public Object retrieve(User user, @PathVariable long id) {
		PerfScene perfScene = perfSceneService.retrieve(id);
		if (perfScene == null)
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		return perfScene;
	}

	@RequestMapping(value = {"", "/"}, method = RequestMethod.POST, consumes = "application/json")
	public Object create(User user, @RequestBody PerfScene perfScene) {
		ResponseEntity<Object> responseEntity ;
		try {
			LOGGER.info(user.getUserName()+" == Request create scene :"+mapper.writeValueAsString(perfScene));
			PerfScene sceneNew = perfSceneService.create(user,perfScene);
			if (sceneNew != null){
				return  sceneNew;
			}else {
				responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
				return responseEntity;
			}
		}catch (Exception e){
			LOGGER.error(e.getMessage());
			if(e instanceof CreateScriptException){
				responseEntity = new ResponseEntity<>(e.getMessage(),null,HttpStatus.INTERNAL_SERVER_ERROR);
			}else {
				responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			e.printStackTrace();
		}
		return responseEntity;
	}

	@RequestMapping(value = {"/{id}", "/{id}/"}, method = RequestMethod.PUT, consumes = "application/json")
	public Object update(User user, @PathVariable long id, @RequestBody PerfScene perfScene) {

		ResponseEntity<Object> responseEntity = null;
		try {
			LOGGER.info(user.getUserName()+" == Request update scene :"+mapper.writeValueAsString(perfScene));
			PerfScene tempPerfScene = perfSceneService.retrieve(id);
			if (tempPerfScene == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}else {
				return perfSceneService.update(user, id, perfScene);
			}
		}catch (Exception e){
			LOGGER.error(e.getMessage());
			if(e instanceof CreateScriptException){
				responseEntity = new ResponseEntity<>(e.getMessage(),null,HttpStatus.INTERNAL_SERVER_ERROR);
			}else {
				responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			e.printStackTrace();
		}
		return responseEntity;
	}

	/**
	 * 压测场景删除
	 * @param user
	 * @param id
	 * @return
	 */
	@RequestMapping(value = {"/{id}", "/{id}/"}, method = RequestMethod.DELETE)
	public Object delete(User user, @PathVariable long id) {
		PerfScene perfScene = perfSceneService.retrieve(id);
		if (perfScene == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		perfSceneService.delete(user, id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * 压测场景批量删除
	 * @param user
	 * @param ids
	 * @return
	 */
	@RequestMapping(value = {"", "/"}, method = RequestMethod.DELETE)
	public Object batchDelete(User user, @RequestParam String ids) {
		if (ids == null || ids.length() <= 0)
			return new ResponseEntity<>("Bad request, no perfscene to delete.", HttpStatus.BAD_REQUEST);

		for (String idStr : StringUtils.split(ids, ",")) {
			perfSceneService.delete(user, NumberUtils.toLong(idStr, 0));
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

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

package com.huawei.argus.restcontroller;

import com.alibaba.fastjson.JSONObject;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.operation.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/operation/announcement")
@PreAuthorize("hasAnyRole('A', 'S')")
public class RestAnnouncementController extends RestBaseController {

	@Autowired
	private AnnouncementService announcementService;

	/**
	 * Open the announcement editor.
	 *
	 * @return operation/announcement
	 */
	@RequestMapping(value = "", method = RequestMethod.GET)
	public JSONObject open() {
		String announcement = announcementService.getOne();
		JSONObject modelInfos = new JSONObject();
		modelInfos.put("announcement", announcement);
		modelInfos.put("content", announcement);
		return modelInfos;
	}

	/**
	 * Save the announcement.
	 *
	 * @param content
	 *            new announcement content
	 * @return operation/announcement
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	public JSONObject save(@RequestParam final String content) {
		JSONObject modelInfos = new JSONObject();
		modelInfos.put("success", announcementService.save(content));
		String announcement = announcementService.getOne();
		modelInfos.put("announcement", announcement);
		modelInfos.put("content", announcement);
		return modelInfos;
	}
}


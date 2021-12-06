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

import org.ngrinder.agent.service.AgentPackageService;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.util.FileDownloadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

import java.io.File;

import static org.ngrinder.common.util.ExceptionUtils.processException;

@RestController
@RequestMapping("/rest/monitor")
public class RestMonitorDownloadController extends RestBaseController {

	@Autowired
	private AgentPackageService agentPackageService;


	/**
	 * Download monitor.
	 *
	 * @param fileName monitor file name.
	 * @param response response.
	 */

	@RequestMapping(value = "/download/{fileName:[a-zA-Z0-9\\.\\-_]+}")
	public void download(@PathVariable String fileName, HttpServletResponse response) {
		File home = getConfig().getHome().getDownloadDirectory();
		File monitorFile = new File(home, fileName);
		FileDownloadUtils.downloadFile(response, monitorFile);
	}

	/**
	 * Download monitor.
	 *
	 */
	@RequestMapping(value = "/download")
	public String download() {
		try {
			final File monitorPackage = agentPackageService.createMonitorPackage();
			return monitorPackage.getName();
		} catch (Exception e) {
			throw processException(e);
		}
	}

}

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

package com.huawei.argus.testreport.controller;

import com.alibaba.fastjson.JSONObject;
import com.huawei.argus.restcontroller.RestBaseController;
import com.huawei.argus.testreport.service.ITestReportService;
import org.apache.commons.lang.math.NumberUtils;
import org.ngrinder.model.TestReport;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.ngrinder.common.util.ObjectUtils.defaultIfNull;


@RestController
@RequestMapping("/rest/testReport")
public class TestReportController extends RestBaseController {

	@Autowired
	private ITestReportService testReportService;

	@RequestMapping("/save")
	public String create(User user, @RequestParam String testReportInfos) {
		if (StringUtils.isEmpty(testReportInfos)) {
			return returnError("压测报告信息缺失不完善");
		}
		TestReport testReport = JSONObject.parseObject(testReportInfos, TestReport.class);
		testReport.setCreatedUser(user);
		testReport = testReportService.save(testReport);
		return testReport.toString();
	}

	@RequestMapping("/list")
	public JSONObject getPagedAll(User user, @RequestParam(required = false) String query,
								  @RequestParam(required = false) String testType,
								  @RequestParam(required = false) String testNames,
								  @RequestParam(required = false) String startTime,
								  @RequestParam(required = false) String endTime,
								  @RequestParam(required = false) String pages) {
		Pageable pageable = getPageable(pages);
		pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(),
			defaultIfNull(pageable.getSort(),
				new Sort(Sort.Direction.DESC, "id")));
		Page<TestReport> testReports = testReportService.getPagedAll(user, query, testType,testNames, startTime, endTime, pageable);
		if (testReports.getNumberOfElements() == 0) {
			pageable = new PageRequest(0, pageable.getPageSize(), defaultIfNull(pageable.getSort(),
				new Sort(Sort.Direction.DESC, "id")));
			testReports = testReportService.getPagedAll(user, query, testType, testNames, startTime, endTime, pageable);
		}
		JSONObject modelInfos = new JSONObject();
		modelInfos.put("testReportListPage", pageToJson(testReports));
		putPageIntoModelMap(modelInfos, pageable);
		return modelInfos;
	}


	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public String delete(User user, @RequestParam("ids") String ids) {
		for (String idStr : org.apache.commons.lang.StringUtils.split(ids, ",")) {
			testReportService.delete(user, NumberUtils.toLong(idStr, 0));
		}
		return returnSuccess();
	}

}

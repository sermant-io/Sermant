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

package com.huawei.argus.report.controller;

import com.alibaba.fastjson.JSONObject;
import com.huawei.argus.report.service.PerfTestReportService;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.model.PerfTestReport;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.ngrinder.common.util.ObjectUtils.defaultIfNull;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Controller
@RequestMapping("/perfreport")
public class PerfTestReportController extends BaseController {

	private static final String DEFAULT_PAGE = "1";
	private static final String DEFAULT_PAGE_SIZE = "10";

	@Autowired
	private PerfTestReportService perfTestReportService;


	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public @ResponseBody
	Page<PerfTestReport> getBasicReport(User user,
										@PageableDefault(page = 0, size = 10) Pageable pageable,
										@RequestParam(required = false) String query) {

		pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(),
			defaultIfNull(pageable.getSort(),
				new Sort(Sort.Direction.DESC, "id")));
		Page<PerfTestReport> BasicReportByUserId = perfTestReportService.getBasicReportByUserId(pageable, user,query);

		//	Page<PerfTestReport> BasicReportPage = perfTestReportService.getApiBasicReport(pageable);
		//return toJsonHttpEntity(basicReports);
		return BasicReportByUserId;
	}


	@RequestMapping(value = {"/graph/{id}", "/{id}/"}, method = RequestMethod.GET)
	public @ResponseBody
	JSONObject getGraphReportByReportId(@PathVariable Long id) {

		//根据报告id查询当前的图表数据
		JSONObject thisperfGraphData = perfTestReportService.getPerfGraphDataByReportId(id);
		return thisperfGraphData;
	}

	@RequestMapping(value = {"/basic/{id}", "/{id}/"}, method = RequestMethod.GET)
	public @ResponseBody
	JSONObject getBasicReportByReportId(@PathVariable Long id) {

		//根据报告id查询当前的基本数据
		//PerfTestReport thisPerfTestReport = perfTestReportService.getBasicReportByReportId(id);
		JSONObject allReportByReportId = perfTestReportService.getAllReportByReportId(id);

		return allReportByReportId;
	}


	@RequestMapping(value = {"/{id}", "/{id}/"}, method = RequestMethod.DELETE)
	public @ResponseBody
	Object deleteBasicReportByReportId(@PathVariable Long id) {
		perfTestReportService.deleteBasicReportByReportId(id);
		return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(value = "/hosts/{id}", method = RequestMethod.GET)
	@ResponseBody
	public String[] getTargetHosts(User user, @PathVariable(value = "id") long id) {
		//PerfTest perfTest = perfTestService.getOne(user, id);
		PerfTestReport thisPerfTestReport = perfTestReportService.getBasicReportByReportId(id);
		List<String> lstHosts = thisPerfTestReport.getTargetHostIP();
		return lstHosts.toArray(new String[lstHosts.size()]);
	}

//	//测试用
//	@RequestMapping(value = {"/saveReport"}, method = RequestMethod.GET)
//	@ResponseBody
//	public String test(User user) {
//		PerfTestReport perfTestReport = perfTestReportService.saveApiBasicReport(7l, 600, user);
//		perfTestReportService.savePerfGraph(7l, perfTestReport, "TPS,Errors,Mean_Test_Time_(ms),Mean_time_to_first_byte,User_defined,Vuser", false, 600);
//		System.out.println("执行成功");
//		return "基本报告数据和图表数据持久化成功";
//	}
}

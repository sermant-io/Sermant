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

package com.huawei.argus.perftest.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.huawei.argus.perftest.service.IAgentInfoService;
import com.huawei.argus.perftest.service.IPerfTestTaskService;
import com.huawei.argus.perftest.util.PerfTestUtil;
import com.huawei.argus.scene.service.PerfSceneService;
import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.constants.GrinderConstants;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.common.util.DateUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.*;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.ngrinder.common.util.ObjectUtils.defaultIfNull;

@Api
@SuppressWarnings("SpringJavaAutowiringInspection")
@RestController
@RequestMapping("/perftest/task")
public class PerfTestTaskController extends BaseController {

	@Autowired
	private IPerfTestTaskService perfTestTaskService;
	@Autowired
	private IAgentInfoService agetnInfoService;
	@Autowired
	private AgentManagerService agentManagerService;
	@Autowired
	private PerfSceneService perfSceneService;

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private AgentManager agentManager;


	@Autowired
	private TagService tagService;

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;

	@Autowired
	private UserService userService;

	@Autowired
	private RegionService regionService;

	private Gson fileEntryGson;

	@RequestMapping(value =  {"/list", "/", ""},method = RequestMethod.GET)
	public Object getAll(User user, @RequestParam(required = false) String query,
							 @RequestParam(required = false) String tag, @RequestParam(required = false) String queryFilter,
							 @PageableDefault(page = 1, size = 10) Pageable pageable, ModelMap model) {
		pageable = new PageRequest(pageable.getPageNumber()-1, pageable.getPageSize(),
			defaultIfNull(pageable.getSort(),
				new Sort(Sort.Direction.DESC, "id")));
		Page<PerfTest> tests = perfTestTaskService.getPagedAll(user, query, tag, queryFilter, pageable);
		if (tests.getNumberOfElements() == 0) {
			pageable = new PageRequest(0, pageable.getPageSize(), defaultIfNull(pageable.getSort(),
				new Sort(Sort.Direction.DESC, "id")));
			tests = perfTestTaskService.getPagedAll(user, query, tag, queryFilter, pageable);
		}
		annotateDateMarker(tests);
		List<PerfTest> content = tests.getContent();
		for (PerfTest perfTest : content) {
			perfTest.setUserId(perfTest.getLastModifiedUser().getUserId());
			perfTest.setCreatedUser(null);
			perfTest.setLastModifiedUser(null);

		}

		model.addAttribute("tag", tag);
		model.addAttribute("availTags", tagService.getAllTagStrings(user, StringUtils.EMPTY));
		model.addAttribute("testList", tests.getContent());
		model.addAttribute("testUser",user.getUserName());
		model.addAttribute("totalPage", tests.getTotalPages());
		model.addAttribute("totalCount", tests.getTotalElements());
		model.addAttribute("queryFilter", queryFilter);
		model.addAttribute("query", query);
		putPageIntoModelMap(model, pageable);
//		HashMap hashMap = new HashMap();
		JSONObject hashMap = new JSONObject();
		hashMap.put("tag", tag);
		hashMap.put("availTags", tagService.getAllTagStrings(user, StringUtils.EMPTY));
		hashMap.put("queryFilter", queryFilter);
		hashMap.put("query", query);
		hashMap.put("testUser",user.getUserName());
		hashMap.put("testList", content);
		hashMap.put("totalPage", tests.getTotalPages());
		hashMap.put("totalCount", tests.getTotalElements());
		return hashMap;
	}



	/**
	 * 删除多压测任务
	 * @param user
	 * @param ids
	 * @return
	 */
	@RequestMapping(value = {""}, method = RequestMethod.DELETE)
	public HttpEntity<String> delete(User user, @RequestParam(value = "ids", defaultValue = "") String ids) {
		if(StringUtils.isBlank(ids)){
			return errorJsonHttpEntity();
		}
		perfTestTaskService.deletePerfTests(user,ids);
		return successJsonHttpEntity();
	}

	/**
	 *	查询指定压测任务，用于查看
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public HttpEntity getOne(User user, @PathVariable("id") Long id,ModelMap model) {
		PerfTest perfTest = perfTestTaskService.getPerfTestTask(user, id,true);
		if(perfTest==null){
			return null;
		}
		PerfScene perfScene = perfTest.getPerfScene();
//		Object[] objects  = new Object[]{
//			perfScene
//		};
//		List<HashMap> agentsList = null;
//		if (perfTest.getAgentIds() != null && perfTest.getAgentIds().length() > 0) {
//			Collection<AgentInfo> agents = agetnInfoService.getSelectedAgents(perfTest.getAgentIds());
//			agentsList = agentCollectionToList(agents);
//		}
		model.clear();
		model.addAttribute("perfTest",perfTest);
		model.addAttribute(PARAM_LOG_LIST, perfTestTaskService.getLogFiles(id));
//		model.addAttribute("monitoringConfig",perfTest.getMonitoringConfig());
//		model.addAttribute("perfScene",perfScene);
//		model.addAttribute("perfScenes",objects);
//		model.addAttribute("agentsList", agentsList);
//		model.put("perf", perfTestTaskService.getStatistics(perfTest));
		return toJsonHttpEntity(model);
	}

	/**
	 *	查询指定压测任务，用于查看或者编辑
	 */
	@RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Object editOne(User user, @PathVariable("id") Long id,ModelMap model) {
		PerfTest perfTest = perfTestTaskService.getPerfTestTask(user, id,true);
		if(perfTest==null){
			return null;
		}
		HashMap hashMap = new HashMap();
		hashMap.put("perfTest", PerfTestUtil.perfTestToVo(perfTest));
		return hashMap;
	}

	@RequestMapping(value = "/reportId/{id}", method = RequestMethod.GET)
	public HttpEntity getReportId(User user, @PathVariable("id") Long id) {
		PerfTest perfTest = perfTestTaskService.getPerfTestTask(user, id,true);
		if(perfTest==null){
			return null;
		}
		return toJsonHttpEntity(perfTest.getPerfTestReportId());
	}



	@RequestMapping(value = "/agent",method = RequestMethod.GET)
	public HttpEntity getFreeAgents(User user, @RequestParam(value = "region", required = false) final String region,ModelMap model) {
		/**
		 * 获取可用的agents信息
		 */
		model.clear();
		Collection<AgentInfo> agents  = agetnInfoService.getAgentInfoCollection(user,region);
		List<HashMap> agentsList = agentCollectionToList(agents);
		model.addAttribute("agentsList", agentsList);

		return toJsonHttpEntity(model);
	}
	/**
	 * 获取场景列表
	 * @param user
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/perfScene",method = RequestMethod.GET)
	public HttpEntity getPerfSceneList(User user, ModelMap model) {

		/**
		 * 获取当前用户所创建的场景
		 */
		model.clear();
		List<PerfScene> perfSceneList = perfSceneService.getTaskDataByUser(user);

		model.addAttribute("perfSceneList",perfSceneList);
		return toJsonHttpEntity(model);
	}

	/**
	 * 获取新建压测任务表单及初始化参数
	 * @param user
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/new",method = RequestMethod.GET)
	public HttpEntity openForm(User user, ModelMap model) {

		/**
		 * 获取当前用户所创建的场景
		 */
//		List<Object[]> perfSceneList = perfSceneService.getTaskDataByUser(user);
//		model.addAttribute("perfSceneList",perfSceneList);

		/**
		 * 初始化perfTest信息
		 */
		PerfTest perfTest = new PerfTest();
		perfTest.init();

		model.addAttribute(PARAM_TEST, perfTest);
		Map<String, MutableInt> agentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		model.addAttribute(PARAM_REGION_AGENT_COUNT_MAP, agentCountMap);
		model.addAttribute(PARAM_REGION_LIST, regionService.getAllVisibleRegionNames());
		model.addAttribute(PARAM_PROCESS_THREAD_POLICY_SCRIPT, perfTestTaskService.getProcessAndThreadPolicyScript());

		addDefaultAttributeOnModel(model);

		return toJsonHttpEntity(model);
	}

	/**
	 * 新增、编辑、克隆操作之后的信息保存
	 * @param user
	 * @param perfTest
	 * @param isClone
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	public HttpEntity saveOne(User user, PerfTest perfTest, @RequestParam(value = "perfSceneId", required = false) Long perfSceneId,
							  @RequestParam(value = "isClone", required = false, defaultValue = "false") boolean isClone, ModelMap model) {

		ConcurrentHashMap<String,String> resultMap = validate(user, null, perfTest);
		String errorCode = resultMap.get("errorCode");
		if(errorCode.equals("-1")){
			return toJsonHttpEntity(resultMap);
		}

		// Point to the head revision
		perfTest.setTestName(StringUtils.trimToEmpty(perfTest.getTestName()));
		perfTest.setScriptRevision(-1L);
		perfTest.prepare(isClone);
		PerfScene perfScene = perfSceneService.retrieve(perfSceneId);
		perfTest.setScriptName(perfScene.getScriptPath());
		perfTest.setPerfScene(perfScene);
		perfTest.setCreatedUser(user);
		perfTest.setLastModifiedUser(user);
		perfTest = perfTestTaskService.save(user, perfTest);
		model.clear();
		if (perfTest.getStatus() == Status.SAVED || perfTest.getScheduledTime() != null) {
			//状态是SAVED，非READY，or 定时时间存在，已指定需执行时间，仅进行保存，返回到列表
			return toJsonHttpEntity(perfTest);
		} else {
			//任务执行，返回当前任务信息
			return toJsonHttpEntity(perfTest);
		}
	}

	/**
	 * 开启性能测试任务,
	 * @param user
	 * @param id
	 * @return
	 */
	@RestAPI
	@RequestMapping(value = "/start/{id}", method = RequestMethod.PUT)
	public HttpEntity<String> startOne(User user, @PathVariable("id") Long id) {
		PerfTest one = perfTestTaskService.getOne(id);
		PerfTest perfTest = perfTestTaskService.startPerfTest(user,one);
		if(perfTest==null){
			return errorJsonHttpEntity();
		}
		return toJsonHttpEntity(perfTest);
	}

	/**
	 * 停止性能测试任务，停止多个任务，之间id用英文逗号间隔
	 * @param user
	 * @param ids
	 * @return
	 */
	@RestAPI
	@RequestMapping(value = "/stop", method = RequestMethod.PUT)
	public HttpEntity<String> stopPerfTests(User user, @RequestParam(value = "ids", defaultValue = "") String ids) {
		if (org.apache.commons.lang3.StringUtils.isBlank(ids)){
			return errorJsonHttpEntity();
		}
		perfTestTaskService.stopPerfTests(user,ids);
		return successJsonHttpEntity();
	}


	private void annotateDateMarker(Page<PerfTest> tests) {
		TimeZone userTZ = TimeZone.getTimeZone(getCurrentUser().getTimeZone());
		Calendar userToday = Calendar.getInstance(userTZ);
		Calendar userYesterday = Calendar.getInstance(userTZ);
		userYesterday.add(Calendar.DATE, -1);
		for (PerfTest test : tests) {
			Calendar localedModified = Calendar.getInstance(userTZ);
			localedModified.setTime(DateUtils.convertToUserDate(getCurrentUser().getTimeZone(),
				test.getLastModifiedDate()));
			if (org.apache.commons.lang.time.DateUtils.isSameDay(userToday, localedModified)) {
				test.setDateString("today");
			} else if (org.apache.commons.lang.time.DateUtils.isSameDay(userYesterday, localedModified)) {
				test.setDateString("yesterday");
			} else {
				test.setDateString("earlier");
			}
		}
	}

	public void addDefaultAttributeOnModel(ModelMap model) {
		model.addAttribute(PARAM_AVAILABLE_RAMP_UP_TYPE, RampUp.values());
		model.addAttribute(PARAM_MAX_VUSER_PER_AGENT, agentManager.getMaxVuserPerAgent());
		model.addAttribute(PARAM_MAX_RUN_COUNT, agentManager.getMaxRunCount());
		if (getConfig().isSecurityEnabled()) {
			model.addAttribute(PARAM_SECURITY_LEVEL, getConfig().getSecurityLevel());
		}
		model.addAttribute(PARAM_MAX_RUN_HOUR, agentManager.getMaxRunHour());
		model.addAttribute(PARAM_SAFE_FILE_DISTRIBUTION,
			getConfig().getControllerProperties().getPropertyBoolean(ControllerConstants.PROP_CONTROLLER_SAFE_DIST));
		String timeZone = getCurrentUser().getTimeZone();
		int offset;
		if (StringUtils.isNotBlank(timeZone)) {
			offset = TimeZone.getTimeZone(timeZone).getOffset(System.currentTimeMillis());
		} else {
			offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
		}
		model.addAttribute(PARAM_TIMEZONE_OFFSET, offset);
	}



	@SuppressWarnings("ConstantConditions")
	private ConcurrentHashMap validate(User user, PerfTest oldOne, PerfTest newOne) {
		if (oldOne == null) {
			oldOne = new PerfTest();
			oldOne.init();
		}
		if(StringUtils.isNotBlank(newOne.getTargetHosts())&&newOne.getTargetHosts().equals(":")){
			newOne.setTargetHosts(null);
		}
		ConcurrentHashMap<String,String> resultMap = new ConcurrentHashMap();
		resultMap.put("errorCode","-1");
		newOne = oldOne.merge(newOne);
		String agentIds = newOne.getAgentIds();
		if(StringUtils.isNotBlank(agentIds)){
			newOne.setAgentCount(agentIds.split(",").length);
		}
		if (StringUtils.isEmpty(newOne.getTestName())) {
			resultMap.put("errorMsg","testName should be provided");
			return resultMap;
		}
		boolean flag = false;
		flag = newOne.getStatus().equals(Status.SAVED) || newOne.getStatus().equals(Status.READY);
		if(!flag){
			resultMap.put("errorMsg","status only allows SAVE or READY");
			return resultMap;
		}
		if (newOne.isThresholdRunCount()) {
			final Integer runCount = newOne.getRunCount();
			flag = runCount <= 0 || runCount > agentManager.getMaxRunCount();
			if(flag){
				resultMap.put("errorMsg","runCount should be equal to or less than %s" + agentManager.getMaxRunCount());
				return resultMap;
			}
		} else {
			final Long duration = newOne.getDuration();
			flag = duration <= 0 || duration > (((long) agentManager.getMaxRunHour()) * 3600000L);
			if(flag){
				resultMap.put("errorMsg","duration should be equal to or less than %s" + agentManager.getMaxRunHour());
				return resultMap;
			}

		}
		Map<String, MutableInt> agentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		MutableInt agentCountObj = agentCountMap.get(isClustered() ? newOne.getRegion() : Config.NONE_REGION);
		if(agentCountObj==null){
			resultMap.put("errorMsg","region should be within current region list");
			return resultMap;
		}
		int agentMaxCount = agentCountObj.intValue();
		flag = newOne.getAgentCount() > agentMaxCount;
		if(flag){
			resultMap.put("errorMsg","test agent should be equal to or less than %s" + agentMaxCount);
			return resultMap;
		}
//		if (newOne.getStatus().equals(Status.READY)) {
//			checkArgument(newOne.getAgentCount() >= 1, "agentCount should be more than 1 when it's READY status.");
//		}
//		if(newOne.getStatus().equals(Status.READY) && newOne.getAgentCount()<1){
//			resultMap.put("errorMsg","agentCount should be more than 1 when it's READY status.");
//			return resultMap;
//		}
		if(newOne.getVuserPerAgent() > agentManager.getMaxVuserPerAgent()){
			resultMap.put("errorMsg","vuserPerAgent should be equal to or less than %s" + agentManager.getMaxVuserPerAgent());
			return resultMap;
		}
		if (getConfig().isSecurityEnabled() && GrinderConstants.GRINDER_SECURITY_LEVEL_NORMAL.equals(getConfig().getSecurityLevel()) && StringUtils.isEmpty(newOne.getTargetHosts())) {
			resultMap.put("errorMsg","targetHosts should be provided when security mode is enabled");
			return resultMap;
		}
//		if (newOne.getStatus() != Status.SAVED && StringUtils.isBlank(newOne.getScriptName())) {
//			resultMap.put("errorMsg","scriptName should be provided.");
//			return resultMap;
//		}
//		checkArgument(newOne.getVuserPerAgent() == newOne.getProcesses() * newOne.getThreads(),
//			"vuserPerAgent should be equal to (processes * threads)");

		if(StringUtils.isBlank(newOne.getAgentIds())){
			resultMap.put("errorMsg","agentInfo should be choose");
			return resultMap;
		}
		resultMap.put("errorCode","0");
		return resultMap;
	}

	private List<HashMap> agentCollectionToList(Collection<AgentInfo> agents){
		if (agents == null)
			return null;
		List<HashMap> agentsList = new ArrayList<>();
		for (AgentInfo agent : agents) {
			StringBuilder builder = new StringBuilder(agent.getIp());
			HashMap hashMap = new HashMap(2);
			hashMap.put("id",agent.getId());
			hashMap.put("agentName",builder.append("_").append(agent.getHostName()));
			agentsList.add(hashMap);
		}
		return agentsList;
	}
}

package com.huawei.argus.restcontroller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.argus.report.service.TpsCalculateService;
import com.huawei.argus.scenario.service.IScenarioPerfTestService;
import com.huawei.argus.scenario.service.IScenarioService;
import net.grinder.util.LogCompressUtils;
import net.grinder.util.Pair;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.constants.GrinderConstants;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.common.util.DateUtils;
import org.ngrinder.common.util.FileDownloadUtils;
import org.ngrinder.infra.config.Config;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.*;
import org.ngrinder.perftest.service.AgentManager;
import org.ngrinder.perftest.service.PerfTestService;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.FileCategory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.user.service.UserService;
import org.python.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.trimToEmpty;
import static org.ngrinder.common.util.CollectionUtils.buildMap;
import static org.ngrinder.common.util.CollectionUtils.newHashMap;
import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.ObjectUtils.defaultIfNull;
import static org.ngrinder.common.util.Preconditions.*;
import static org.ngrinder.common.util.Preconditions.checkArgument;

@RestController
@RequestMapping("/rest/perftest")
public class RestPerftestController extends RestBaseController {

	@Autowired
	private PerfTestService perfTestService;

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private AgentManager agentManager;

	@Autowired
	private AgentManagerService agentManagerService;

	@Autowired
	private TagService tagService;

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;

	@Autowired
	private UserService userService;

	@Autowired
	private RegionService regionService;

	private Gson fileEntryGson;

	private Gson gson = new Gson();

	@Autowired
	private IScenarioPerfTestService scenarioPerfTestService;

	@Autowired
	IScenarioService scenarioService;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void init() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(FileEntry.class, new FileEntry.FileEntrySerializer());
		fileEntryGson = gsonBuilder.create();
	}

	/**
	 * Get the perf test lists.
	 *
	 * @param user        user
	 * @param query       query string to search the perf test
	 * @param tag         tag
	 * @param queryFilter "F" means get only finished, "S" means get only scheduled tests.
	 * @param pageable    page
	 * @return perftest/list
	 */
	@RequestMapping({"/list", "/", ""})
	public JSONObject getAll(User user, @RequestParam(required = false) String query,
							 @RequestParam(required = false) String tag, @RequestParam(required = false) String queryFilter,
							 @PageableDefault(page = 0, size = 10) Pageable pageable,
							 @RequestParam(required = false) String testName,
							 @RequestParam(required = false) String testType,
							 @RequestParam(required = false) String scriptPath,
							 @RequestParam(required = false) String owner) {
		pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(),
			defaultIfNull(pageable.getSort(),
				new Sort(Sort.Direction.DESC, "id")));
		Page<PerfTest> tests = perfTestService.getPagedAll(user, query, tag, queryFilter, pageable, testName, testType, scriptPath, owner);
		if (tests.getNumberOfElements() == 0) {
			pageable = new PageRequest(0, pageable.getPageSize(), defaultIfNull(pageable.getSort(),
				new Sort(Sort.Direction.DESC, "id")));
			tests = perfTestService.getPagedAll(user, query, tag, queryFilter, pageable, testName, testType, scriptPath, owner);
		}
		annotateDateMarker(tests);
		JSONObject pageInfo = new JSONObject();
		pageInfo.put("total", tests.getTotalElements());
		pageInfo.put("totalPages", tests.getTotalPages());
		pageInfo.put("content", tests.getContent());
		JSONObject modelInfos = new JSONObject();
		modelInfos.put("tag", tag);
		modelInfos.put("availTags", tagService.getAllTagStrings(user, StringUtils.EMPTY));
		modelInfos.put("testListPage", pageInfo);
		modelInfos.put("queryFilter", queryFilter);
		modelInfos.put("query", query);
/*
		putPageIntoModelMap(modelInfos, pageable);
		// 查询压测场景信息
		Iterator<Object> content = testListPage.getJSONArray("content").stream().iterator();
		while (content.hasNext()) {
			JSONObject next = (JSONObject) content.next();
			Long perfTestId = next.getLong("id");
			List<ScenarioPerfTest> allByID = scenarioPerfTestService.getAllByID(user, perfTestId, null);
			if (allByID != null && !allByID.isEmpty()) {
				Long scenarioId = allByID.get(0).getScenarioId();
				Scenario scenario = scenarioService.getOne(scenarioId);
				if (scenario != null) {
					next.put("scenario", modelStrToJson(scenario.toString()));
					next.put("scenarioType", scenario.getScenarioType());
				}
			}
		}
*/

		return modelInfos;
	}

	@RequestMapping({"/alllist"})
	public JSONObject getAll(User user, @RequestParam(required = false) String query,
							 @RequestParam(required = false) String tag, @RequestParam(required = false) String queryFilter,
							 @RequestParam(required = false) String pages,
							 @RequestParam(required = false) String testName,
							 @RequestParam(required = false) String testType,
							 @RequestParam(required = false) String scriptPath,
							 @RequestParam(required = false) String owner) {
		return getAll(user, query, tag, queryFilter, getPageable(pages), testName, testType, scriptPath, owner);
	}

	/**
	 * 查询压测任务的标签
	 *
	 * @param user  当前用户
	 * @param query 查询关键字
	 * @return
	 */
	@RequestMapping({"/allTags"})
	public List<String> getAllTags(User user, @RequestParam(required = false) String query) {
		List<String> allTags = tagService.getAllTagStringsByKeywords(user, query);
		return allTags == null ? Collections.emptyList() : allTags;
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

	/**
	 * Open the new perf test creation form.
	 *
	 * @param user user
	 * @return "perftest/detail"
	 */
	@RequestMapping("/new")
	public JSONObject openForm(User user) {
		return getOne(user, null);
	}

	/**
	 * Get the perf test detail on the given perf test id.
	 *
	 * @param user user
	 * @param id   perf test id
	 * @return perftest/detail
	 */
	@RequestMapping("/perfTestId")
	public JSONObject getOne(User user, @RequestParam Long id) {
		PerfTest test = null;
		if (id != null) {
			test = getOneWithPermissionCheck(user, id, true);
		}

		if (test == null) {
			test = new PerfTest(user);
			test.init();
		}
		JSONObject modelInfos = new JSONObject();
		modelInfos.put(PARAM_TEST, test);
		// Retrieve the agent count map based on create user, if the test is
		// created by the others.
		user = test.getCreatedUser() != null ? test.getCreatedUser() : user;

		Map<String, MutableInt> agentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		modelInfos.put(PARAM_REGION_AGENT_COUNT_MAP, agentCountMap);
		modelInfos.put(PARAM_REGION_LIST, regionService.getAllVisibleRegionNames());
		modelInfos.put(PARAM_PROCESS_THREAD_POLICY_SCRIPT, perfTestService.getProcessAndThreadPolicyScript());
		addDefaultAttributeOnModel(modelInfos);
		return modelInfos;
	}

	private ArrayList<String> getRegions(Map<String, MutableInt> agentCountMap) {
		ArrayList<String> regions = new ArrayList<String>(agentCountMap.keySet());
		Collections.sort(regions);
		return regions;
	}


	/**
	 * Search tags based on the given query.
	 *
	 * @param user  user to search
	 * @param query query string
	 * @return found tag list in json
	 */
	@RequestMapping("/search_tag")
	public HttpEntity<String> searchTag(User user, @RequestParam(required = false) String query) {
		List<String> allStrings = tagService.getAllTagStrings(user, query);
		if (StringUtils.isNotBlank(query)) {
			allStrings.add(query);
		}
		return toJsonHttpEntity(allStrings);
	}

	/**
	 * Add the various default configuration values on the model.
	 *
	 * @param modelInfos model to which put the default values
	 */
	public void addDefaultAttributeOnModel(Map<String, Object> modelInfos) {
		modelInfos.put(PARAM_AVAILABLE_RAMP_UP_TYPE, RampUp.values());
		modelInfos.put(PARAM_MAX_VUSER_PER_AGENT, agentManager.getMaxVuserPerAgent());
		modelInfos.put(PARAM_MAX_RUN_COUNT, agentManager.getMaxRunCount());
		if (getConfig().isSecurityEnabled()) {
			modelInfos.put(PARAM_SECURITY_LEVEL, getConfig().getSecurityLevel());
		}
		modelInfos.put(PARAM_MAX_RUN_HOUR, agentManager.getMaxRunHour());
		modelInfos.put(PARAM_SAFE_FILE_DISTRIBUTION,
			getConfig().getControllerProperties().getPropertyBoolean(ControllerConstants.PROP_CONTROLLER_SAFE_DIST));
		String timeZone = getCurrentUser().getTimeZone();
		int offset;
		if (StringUtils.isNotBlank(timeZone)) {
			offset = TimeZone.getTimeZone(timeZone).getOffset(System.currentTimeMillis());
		} else {
			offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
		}
		modelInfos.put(PARAM_TIMEZONE_OFFSET, offset);
	}

	/**
	 * Get the perf test creation form for quickStart.
	 *
	 * @param user       user
	 * @param urlString  URL string to be tested.
	 * @param scriptType scriptType
	 * @return perftest/detail
	 */
	@RequestMapping("/quickstart")
	public JSONObject getQuickStart(User user,
									@RequestParam(value = "url", required = true) String urlString,
									@RequestParam(value = "scriptType", required = true) String scriptType) {
		URL url = checkValidURL(urlString);
		FileEntry newEntry = fileEntryService.prepareNewEntryForQuickTest(user, urlString,
			scriptHandlerFactory.getHandler(scriptType));
		JSONObject modelInfos = new JSONObject();
		modelInfos.put(PARAM_QUICK_SCRIPT, newEntry.getPath());
		modelInfos.put(PARAM_QUICK_SCRIPT_REVISION, newEntry.getRevision());
		modelInfos.put(PARAM_TEST, createPerfTestFromQuickStart(user, "Test for " + url.getHost(), url.getHost()).toString());
		Map<String, MutableInt> agentCountMap = agentManagerService.getAvailableAgentCountMap(user);

		modelInfos.put(PARAM_REGION_AGENT_COUNT_MAP, modelStrToJson(agentCountMap.toString()));
		modelInfos.put(PARAM_REGION_LIST, getRegions(agentCountMap));
		addDefaultAttributeOnModel(modelInfos);
		modelInfos.put(PARAM_PROCESS_THREAD_POLICY_SCRIPT, perfTestService.getProcessAndThreadPolicyScript());
		return modelInfos;
	}

	/**
	 * Create a new test from quick start mode.
	 *
	 * @param user       user
	 * @param testName   test name
	 * @param targetHost target host
	 * @return test    {@link PerfTest}
	 */
	private PerfTest createPerfTestFromQuickStart(User user, String testName, String targetHost) {
		PerfTest test = new PerfTest(user);
		test.init();
		test.setTestName(testName);
		test.setTargetHosts(targetHost);
		return test;
	}

	/**
	 * Create a new test or cloneTo a current test.
	 *
	 * @param user     user
	 * @param perfTest {@link PerfTest}
	 * @param isClone  true if cloneTo
	 * @return redirect:/perftest/list
	 */
	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public JSONObject saveOne(User user, PerfTest perfTest,
							  @RequestParam(value = "isClone", required = false, defaultValue = "false") boolean isClone) {

		validate(user, null, perfTest);
		// Point to the head revision
		perfTest.setTestName(StringUtils.trimToEmpty(perfTest.getTestName()));
		perfTest.setScriptRevision(-1L);
		perfTest.prepare(isClone);
		Set<MonitoringHost> monitoringHosts = perfTest.getMonitoringHosts();
		if (monitoringHosts != null && !monitoringHosts.isEmpty()) {
			for (MonitoringHost monitoringHost : monitoringHosts) {
				monitoringHost.setPerfTest(perfTest);
				monitoringHost.setId(null);
			}
		}
		perfTest = perfTestService.save(user, perfTest);
		JSONObject modelInfos = new JSONObject();

		modelInfos.put("id", perfTest.getId());
		modelInfos.put("isShowList", false); // true：展示当前脚本运行状态；false：展示列表，当前脚本为运行
		modelInfos.put("status", perfTest.getStatus());
		if (perfTest.getStatus() == Status.SAVED || perfTest.getScheduledTime() != null) {
			modelInfos.put("scheduledTime", perfTest.getScheduledTime());
			modelInfos.put("isShowList", true);
		}
		return modelInfos;
	}

	@RequestMapping(value = "/newPerfTest", method = RequestMethod.POST)
	public JSONObject saveOne(User user, @RequestParam String perfTestInfos,
							  @RequestParam(value = "isClone", required = false, defaultValue = "false") boolean isClone) {

		PerfTest perfTest = JSONObject.parseObject(perfTestInfos, PerfTest.class);
		return saveOne(user, perfTest, isClone);
	}

	@SuppressWarnings("ConstantConditions")
	private void validate(User user, PerfTest oldOne, PerfTest newOne) {
		if (oldOne == null) {
			oldOne = new PerfTest();
			oldOne.init();
		}
		newOne = oldOne.merge(newOne);
		checkNotEmpty(newOne.getTestName(), "testName should be provided");
		checkArgument(newOne.getStatus().equals(Status.READY) || newOne.getStatus().equals(Status.SAVED),
			"status only allows SAVE or READY");
		if (newOne.isThresholdRunCount()) {
			final Integer runCount = newOne.getRunCount();
			checkArgument(runCount > 0 && runCount <= agentManager
					.getMaxRunCount(),
				"runCount should be equal to or less than %s", agentManager.getMaxRunCount());
		} else {
			final Long duration = newOne.getDuration();
			checkArgument(duration > 0 && duration <= (((long) agentManager.getMaxRunHour()) *
					3600000L),
				"duration should be equal to or less than %s", agentManager.getMaxRunHour());
		}
		Map<String, MutableInt> agentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		MutableInt agentCountObj = agentCountMap.get(isClustered() ? newOne.getRegion() : Config.NONE_REGION);
		checkNotNull(agentCountObj, "region should be within current region list");
		int agentMaxCount = agentCountObj.intValue();
		checkArgument(newOne.getAgentCount() <= agentMaxCount, "test agent should be equal to or less than %s",
			agentMaxCount);
		if (newOne.getStatus().equals(Status.READY)) {
			checkArgument(newOne.getAgentCount() >= 1, "agentCount should be more than 1 when it's READY status.");
		}

		checkArgument(newOne.getVuserPerAgent() <= agentManager.getMaxVuserPerAgent(),
			"vuserPerAgent should be equal to or less than %s", agentManager.getMaxVuserPerAgent());
		if (getConfig().isSecurityEnabled() && GrinderConstants.GRINDER_SECURITY_LEVEL_NORMAL.equals(getConfig().getSecurityLevel())) {
			checkArgument(StringUtils.isNotEmpty(newOne.getTargetHosts()),
				"targetHosts should be provided when security mode is enabled");
		}
		if (newOne.getStatus() != Status.SAVED) {
			checkArgument(StringUtils.isNotBlank(newOne.getScriptName()), "scriptName should be provided.");
		}
/*		checkArgument(newOne.getVuserPerAgent() = newOne.getProcesses() * newOne.getThreads(),
			"vuserPerAgent should be less than (processes * threads)");*/
	}

	/**
	 * Leave the comment on the perf test.
	 *
	 * @param id          testId
	 * @param user        user
	 * @param testComment test comment
	 * @param tagString   tagString
	 * @return JSON
	 */
	@RequestMapping(value = "/{id}/leave_comment", method = RequestMethod.POST)
	@ResponseBody
	public String leaveComment(User user, @PathVariable("id") Long id, @RequestParam("testComment") String testComment,
							   @RequestParam(value = "tagString", required = false) String tagString) {
		perfTestService.addCommentOn(user, id, testComment, tagString);
		return returnSuccess();
	}

	@RequestMapping(value = "/leave_comment", method = RequestMethod.POST)
	public String updateLeaveComment(User user, @RequestParam Long id, @RequestParam("testComment") String testComment) {
		PerfTest perfTest = perfTestService.getOne(user, id);
		perfTestService.addCommentOn(user, id, testComment, perfTest.getTagString());
		return returnSuccess();
	}


	private Long[] convertString2Long(String ids) {
		String[] numbers = StringUtils.split(ids, ",");
		Long[] id = new Long[numbers.length];
		int i = 0;
		for (String each : numbers) {
			id[i++] = NumberUtils.toLong(each, 0);
		}
		return id;
	}

	private List<Map<String, Object>> getStatus(List<PerfTest> perfTests) {
		List<Map<String, Object>> statuses = newArrayList();
		for (PerfTest each : perfTests) {
			Map<String, Object> result = newHashMap();
			result.put("id", each.getId());
			result.put("status_id", each.getStatus());
			result.put("status_type", each.getStatus());
			result.put("name", getMessages(each.getStatus().getSpringMessageKey()));
			result.put("icon", each.getStatus().getIconName());
			result.put("message",
				StringUtils.replace(each.getProgressMessage() + "\n<b>" + each.getLastProgressMessage() + "</b>\n"
					+ each.getLastModifiedDateToStr(), "\n", "<br/>"));
			result.put("deletable", each.getStatus().isDeletable());
			result.put("stoppable", each.getStatus().isStoppable());
			result.put("reportable", each.getStatus().isReportable());
			statuses.add(result);
		}
		return statuses;
	}


	/**
	 * Delete the perf tests having given IDs.
	 *
	 * @param user user
	 * @param ids  comma operated IDs
	 * @return success json messages if succeeded.
	 */
	@RestAPI
	@RequestMapping(value = "/api", method = RequestMethod.DELETE)
	public HttpEntity<String> delete(User user, @RequestParam(value = "ids", defaultValue = "") String ids) {
		for (String idStr : StringUtils.split(ids, ",")) {
			perfTestService.delete(user, NumberUtils.toLong(idStr, 0));
			// 删除场景与压测任务的关系
			scenarioPerfTestService.deleteByOneId(user, null, NumberUtils.toLong(idStr, 0), null);
		}
		return successJsonHttpEntity();
	}

	@RestAPI
	@RequestMapping(value = "/deleteReportFile", method = RequestMethod.DELETE)
	public HttpEntity<String> deleteReportFile(User user, @RequestParam(value = "ids", defaultValue = "") String ids) {
		for (String idStr : StringUtils.split(ids, ",")) {
			perfTestService.deleteReportFile(user, NumberUtils.toLong(idStr, 0));
		}
		return successJsonHttpEntity();
	}

	/**
	 * Stop the perf tests having given IDs.
	 *
	 * @param user user
	 * @param ids  comma separated perf test IDs
	 * @return success json if succeeded.
	 */
	@RestAPI
	@RequestMapping(value = "/api/stop", /** params = "action=stop",**/method = RequestMethod.PUT)
	public HttpEntity<String> stop(User user, @RequestParam(value = "ids", defaultValue = "") String ids) {
		for (String idStr : StringUtils.split(ids, ",")) {
			perfTestService.stop(user, NumberUtils.toLong(idStr, 0));
		}
		return successJsonHttpEntity();
	}

	/**
	 * Filter out please_modify_this.com from hosts string.
	 *
	 * @param originalString original string
	 * @return filtered string
	 */
	private String filterHostString(String originalString) {
		List<String> hosts = newArrayList();
		for (String each : StringUtils.split(StringUtils.trimToEmpty(originalString), ",")) {
			if (!each.contains("please_modify_this.com")) {
				hosts.add(each);
			}
		}
		return StringUtils.join(hosts, ",");
	}


	private Map<String, Object> getPerfGraphData(Long id, String[] dataTypes, boolean onlyTotal, int imgWidth) {
		final PerfTest test = perfTestService.getOne(id);
		int interval = perfTestService.getReportDataInterval(id, dataTypes[0], imgWidth);
		Map<String, Object> resultMap = Maps.newHashMap();
		for (String each : dataTypes) {
			Pair<ArrayList<String>, ArrayList<String>> tpsResult = perfTestService.getReportData(id, each, onlyTotal, interval);
			Map<String, Object> dataMap = Maps.newHashMap();
			dataMap.put("labels", tpsResult.getFirst());
			dataMap.put("data", tpsResult.getSecond());
			resultMap.put(StringUtils.replaceChars(each, "()", ""), dataMap);
		}
		resultMap.put(PARAM_TEST_CHART_INTERVAL, interval * test.getSamplingInterval());
		return resultMap;
	}


	/**
	 * Get the running division in perftest configuration page.
	 *
	 * @param user user
	 * @param id   test id
	 * @return perftest/running
	 */
	@RequestMapping(value = "{id}/running_div")
	public JSONObject getReportSection(User user, @PathVariable long id) {
		PerfTest test = getOneWithPermissionCheck(user, id, false);
		JSONObject modelInfos = new JSONObject();
		modelInfos.put(PARAM_TEST, test);
		return modelInfos;
	}


	/**
	 * Get the basic report content in perftest configuration page.
	 * <p/>
	 * This method returns the appropriate points based on the given imgWidth.
	 *
	 * @param user     user
	 * @param id       test id
	 * @param imgWidth image width
	 * @return perftest/basic_report
	 */
	@RequestMapping(value = "{id}/basic_report")
	public JSONObject getReportSection(User user, @PathVariable long id, @RequestParam int imgWidth) {
		PerfTest test = getOneWithPermissionCheck(user, id, false);
		int interval = perfTestService.getReportDataInterval(id, "TPS", imgWidth);
		JSONObject modelInfos = new JSONObject();
		modelInfos.put(PARAM_LOG_LIST, perfTestService.getLogFiles(id));
		modelInfos.put(PARAM_TEST_CHART_INTERVAL, interval * test.getSamplingInterval());
		modelInfos.put(PARAM_TEST, test);
		modelInfos.put(PARAM_TPS, perfTestService.getSingleReportDataAsJson(id, "TPS", interval));
		return modelInfos;
	}

	@RequestMapping(value = "/basic_report")
	public JSONObject getReportSectionById(User user, @RequestParam long id, @RequestParam int imgWidth, @RequestParam int thisDuration, @RequestParam int timeInterval) {
		PerfTest test = getOneWithPermissionCheck(user, id, false);
		int interval = perfTestService.getReportDataInterval(id, "TPS", imgWidth);
		JSONObject modelInfos = new JSONObject();
		modelInfos.put(PARAM_LOG_LIST, perfTestService.getLogFiles(id));
		int sampleInterval = interval * test.getSamplingInterval();
		modelInfos.put(PARAM_TEST_CHART_INTERVAL, sampleInterval);
		modelInfos.put(PARAM_TEST, test);
		TpsCalculateService tpsCalculateService = new TpsCalculateService();
		tpsCalculateService.setResultSampleInterval(timeInterval)
			.setTestSampleInterval(test.getSamplingInterval())
			.setResultShowTime(thisDuration)
			.setNeededExecuteTime(test.getDuration())
			.setTestStartTime(test.getStartTime().getTime())
			.isRunning(test.getStatus() == Status.TESTING)
			.setTpsOriginalData(perfTestService.getSingleReportDataAsJson(id, "TPS", interval));
		modelInfos.put(PARAM_TPS, tpsCalculateService.sampleData());
		return modelInfos;
	}

	/**
	 * Download the CSV report for the given perf test id.
	 *
	 * @param user     user
	 * @param id       test id
	 * @param response response
	 */
	@RequestMapping(value = "/{id}/download_csv")
	public void downloadCSV(User user, @PathVariable("id") long id, HttpServletResponse response) {
		PerfTest test = getOneWithPermissionCheck(user, id, false);
		File targetFile = perfTestService.getCsvReportFile(test);
		checkState(targetFile.exists(), "File %s doesn't exist!", targetFile.getName());
		FileDownloadUtils.downloadFile(response, targetFile);
	}

	/**
	 * Download logs for the perf test having the given id.
	 *
	 * @param user     user
	 * @param id       test id
	 * @param path     path in the log folder
	 * @param response response
	 */
	@RequestMapping(value = "/{id}/download_log/**")
	public void downloadLog(User user, @PathVariable("id") long id, @RemainedPath String path,
							HttpServletResponse response) {
		getOneWithPermissionCheck(user, id, false);
		File targetFile = perfTestService.getLogFile(id, path);
		FileDownloadUtils.downloadFile(response, targetFile);
	}

	@RequestMapping(value = "/download_log")
	public JSONObject downloadLogByID(User user, @RequestParam long id, @RequestParam String path) {
		getOneWithPermissionCheck(user, id, false);
		File targetFile = perfTestService.getLogFile(id, path);
		JSONObject logFile = new JSONObject();
		if (targetFile == null) {
			logFile.put(JSON_SUCCESS, false);
			return logFile;
		}
		try {
			logFile.put(
				"Content-Disposition",
				"attachment;filename=" + targetFile.getName());
			logFile.put("contentType", "application/octet-stream; charset=UTF-8");
			logFile.put("Content-Length", "" + targetFile.length());
			ByteArrayInputStream fis = null;
			logFile.put("content", Files.readAllBytes(targetFile.toPath()));
			logFile.put(JSON_SUCCESS, true);
		} catch (IOException e) {
			CoreLogger.LOGGER.error("Error while download log. {}", logFile,  e);
		}

		return logFile;
	}

	/**
	 * Show the given log for the perf test having the given id.
	 *
	 * @param user     user
	 * @param id       test id
	 * @param path     path in the log folder
	 * @param response response
	 */
	@RequestMapping(value = "/{id}/show_log/**")
	public void showLog(User user, @PathVariable("id") long id, @RemainedPath String path, HttpServletResponse response) {
		getOneWithPermissionCheck(user, id, false);
		File targetFile = perfTestService.getLogFile(id, path);
		response.reset();
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(targetFile);
			ServletOutputStream outputStream = response.getOutputStream();
			if (FilenameUtils.isExtension(targetFile.getName(), "zip")) {
				// Limit log view to 1MB
				outputStream.println(" Only the last 1MB of a log shows.\n");
				outputStream.println("==========================================================================\n\n");
				LogCompressUtils.decompress(fileInputStream, outputStream, 1 * 1024 * 1204);
			} else {
				IOUtils.copy(fileInputStream, outputStream);
			}
		} catch (Exception e) {
			CoreLogger.LOGGER.error("Error while processing log. {}", targetFile, e);
		} finally {
			IOUtils.closeQuietly(fileInputStream);
		}
	}

	/**
	 * Get the running perf test info having the given id.
	 *
	 * @param user user
	 * @param id   test id
	 * @return JSON message	containing test,agent and monitor status.
	 */
	@RequestMapping(value = "/{id}/api/sample")
	@RestAPI
	public HttpEntity<String> refreshTestRunning(User user, @PathVariable("id") long id) {
		PerfTest test = checkNotNull(getOneWithPermissionCheck(user, id, false), "given test should be exist : " + id);
		Map<String, Object> map = newHashMap();
		map.put("status", test.getStatus());
		map.put("perf", perfTestService.getStatistics(test));
		map.put("agent", perfTestService.getAgentStat(test));
		map.put("monitor", perfTestService.getMonitorStat(test));
		return toJsonHttpEntity(map);
	}

	@RequestMapping(value = "/api/sample")
	public HttpEntity<Map<String, Object>> refreshTestRunningById(User user, @RequestParam long id) {
		PerfTest test = checkNotNull(getOneWithPermissionCheck(user, id, false), "given test should be exist : " + id);
		Map<String, Object> map = newHashMap();
		map.put("test", test);
		map.put("status", test.getStatus());
		map.put("perf", perfTestService.getStatistics(test));
		map.put("agent", perfTestService.getAgentStat(test));
		map.put("monitor", perfTestService.getMonitorStat(test));
		map.put(PARAM_LOG_LIST, perfTestService.getLogFiles(id));
		return new HttpEntity<>(map);
	}

	/**
	 * Get the detailed perf test report.
	 *
	 * @param id test id
	 * @return perftest/detail_report
	 */
	@SuppressWarnings("MVCPathVariableInspection")
	@RequestMapping(value = {"/{id}/detail_report", /** for backward compatibility */"/{id}/report"})
	public JSONObject getReport(@PathVariable("id") long id) {
		JSONObject modelInfos = new JSONObject();
		PerfTest test = perfTestService.getOne(id);
		if (test != null) {
			modelInfos.put("test", test);
		}
		modelInfos.put("plugins", perfTestService.getAvailableReportPlugins(id));
		return modelInfos;
	}

	@RequestMapping(value = {"/detail_report", /** for backward compatibility */"/{id}/report"})
	public JSONObject getReportById(@RequestParam long id) {
		JSONObject modelInfos = new JSONObject();
		PerfTest test = perfTestService.getOne(id);
		if (test == null) {
			return modelInfos;
		}
		modelInfos.put("test", test);
		modelInfos.put("plugins", perfTestService.getAvailableReportPlugins(id));
		return modelInfos;
	}

	/**
	 * Get the detailed perf test report.[未改变，不使用]
	 *
	 * @param id test id
	 * @return perftest/detail_report/perf
	 */
	@SuppressWarnings({"MVCPathVariableInspection", "UnusedParameters"})
	@RequestMapping("/{id}/detail_report/perf")
	public String getDetailPerfReport(@PathVariable("id") long id) {
		return "perftest/detail_report/perf";
	}

	/**
	 * Get the detailed perf test monitor report.[未改变，不使用]
	 *
	 * @param id       test id
	 * @param targetIP target ip
	 * @param modelMap model map
	 * @return perftest/detail_report/monitor
	 */
	@SuppressWarnings("UnusedParameters")
	@RequestMapping("/{id}/detail_report/monitor")
	public String getDetailMonitorReport(@PathVariable("id") long id, @RequestParam("targetIP") String targetIP,
										 ModelMap modelMap) {
		modelMap.addAttribute("targetIP", targetIP);
		return "perftest/detail_report/monitor";
	}

	/**
	 * Get the detailed perf test report.
	 *
	 * @param id       test id
	 * @param plugin   test report plugin category[未改变，不使用]
	 * @param modelMap model map
	 * @return perftest/detail_report/plugin
	 */
	@SuppressWarnings("UnusedParameters")
	@RequestMapping("/{id}/detail_report/plugin/{plugin}")
	public String getDetailPluginReport(@PathVariable("id") long id,
										@PathVariable("plugin") String plugin, @RequestParam("kind") String kind, ModelMap modelMap) {
		modelMap.addAttribute("plugin", plugin);
		modelMap.addAttribute("kind", kind);
		return "perftest/detail_report/plugin";
	}


	private PerfTest getOneWithPermissionCheck(User user, Long id, boolean withTag) {
		PerfTest perfTest = withTag ? perfTestService.getOneWithTag(id) : perfTestService.getOne(id);
		if (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.SUPER_USER)) {
			return perfTest;
		}
		if (perfTest != null && !user.equals(perfTest.getCreatedUser())) {
			throw processException("User " + user.getUserId() + " has no right on PerfTest " + id);
		}
		return perfTest;
	}


	private Map<String, String> getMonitorGraphData(long id, String targetIP, int imgWidth) {
		int interval = perfTestService.getMonitorGraphInterval(id, targetIP, imgWidth);
		Map<String, String> sysMonitorMap = perfTestService.getMonitorGraph(id, targetIP, interval);
		PerfTest perfTest = perfTestService.getOne(id);
		sysMonitorMap.put("interval", String.valueOf(interval * (perfTest != null ? perfTest.getSamplingInterval() : 1)));
		return sysMonitorMap;
	}


	/**
	 * Get the count of currently running perf test and the detailed progress info for the given perf test IDs.
	 *
	 * @param user user
	 * @param ids  comma separated perf test list
	 * @return JSON message containing perf test status
	 */
	@RestAPI
	@RequestMapping("/api/status")
	public HttpEntity<String> getStatuses(User user, @RequestParam(value = "ids", defaultValue = "") String ids) {
		List<PerfTest> perfTests = perfTestService.getAll(user, convertString2Long(ids));
		return toJsonHttpEntity(buildMap("perfTestInfo", perfTestService.getCurrentPerfTestStatistics(), "status",
			getStatus(perfTests)));
	}

	/**
	 * Get all available scripts in JSON format for the current factual user.
	 *
	 * @param user    user
	 * @param ownerId owner id
	 * @return JSON containing script's list.
	 */
	@RequestMapping("/api/script")
	public HttpEntity<String> getScripts(User user, @RequestParam(value = "ownerId", required = false) String ownerId) {
		if (StringUtils.isNotEmpty(ownerId)) {
			user = userService.getOne(ownerId);
		}
		List<FileEntry> scripts = newArrayList(filter(fileEntryService.getAll(user),
			new com.google.common.base.Predicate<FileEntry>() {
				@Override
				public boolean apply(@Nullable FileEntry input) {
					return input != null && input.getFileType().getFileCategory() == FileCategory.SCRIPT;
				}
			}));
		return toJsonHttpEntity(scripts, fileEntryGson);
	}


	/**
	 * Get resources and lib file list from the same folder with the given script path.
	 *
	 * @param user       user
	 * @param scriptPath script path
	 * @param ownerId    ownerId
	 * @return json string representing resources and libs.
	 */
	@RequestMapping("/api/resource")
	public HttpEntity<String> getResources(User user, @RequestParam String scriptPath,
										   @RequestParam(required = false) String ownerId) {
		if (user.getRole() == Role.ADMIN && StringUtils.isNotBlank(ownerId)) {
			user = userService.getOne(ownerId);
		}
		FileEntry fileEntry = fileEntryService.getOne(user, scriptPath);
		String targetHosts = "";
		List<String> fileStringList = newArrayList();
		if (fileEntry != null) {
			List<FileEntry> fileList = fileEntryService.getScriptHandler(fileEntry).getLibAndResourceEntries(user, fileEntry, -1L);
			for (FileEntry each : fileList) {
				fileStringList.add(each.getPath());
			}
			targetHosts = filterHostString(fileEntry.getProperties().get("targetHosts"));
		}

		return toJsonHttpEntity(buildMap("targetHosts", trimToEmpty(targetHosts), "resources", fileStringList));
	}


	/**
	 * Get the status of the given perf test.
	 *
	 * @param user user
	 * @param id   perftest id
	 * @return JSON message containing perf test status
	 */
	@RestAPI
	@RequestMapping("/api/{id}/status")
	public HttpEntity<String> getStatus(User user, @PathVariable("id") Long id) {
		List<PerfTest> perfTests = perfTestService.getAll(user, new Long[]{id});
		return toJsonHttpEntity(buildMap("status", getStatus(perfTests)));
	}

	/**
	 * Get the logs of the given perf test.
	 *
	 * @param user user
	 * @param id   perftest id
	 * @return JSON message containing log file names
	 */
	@RestAPI
	@RequestMapping("/api/{id}/logs")
	public HttpEntity<String> getLogs(User user, @PathVariable("id") Long id) {
		// Check permission
		getOneWithPermissionCheck(user, id, false);
		return toJsonHttpEntity(perfTestService.getLogFiles(id));
	}

	/**
	 * Get the detailed report graph data for the given perf test id.
	 * This method returns the appropriate points based on the given imgWidth.
	 *
	 * @param id       test id
	 * @param dataType which data
	 * @param imgWidth imageWidth
	 * @return json string.
	 */
	@SuppressWarnings("MVCPathVariableInspection")
	@RestAPI
	@RequestMapping({"/api/{id}/perf", "/api/{id}/graph"})
	public HttpEntity<String> getPerfGraph(@PathVariable("id") long id,
										   @RequestParam(required = true, defaultValue = "") String dataType,
										   @RequestParam(defaultValue = "false") boolean onlyTotal,
										   @RequestParam int imgWidth) {
		String[] dataTypes = checkNotEmpty(StringUtils.split(dataType, ","), "dataType argument should be provided");
		return toJsonHttpEntity(getPerfGraphData(id, dataTypes, onlyTotal, imgWidth));
	}

	@RequestMapping({"/api/perf"})
	public HttpEntity<String> getPerfGraphById(@RequestParam("id") long id,
											   @RequestParam(defaultValue = "") String dataType,
											   @RequestParam(defaultValue = "false") boolean onlyTotal,
											   @RequestParam int imgWidth) {
		String[] dataTypes = checkNotEmpty(StringUtils.split(dataType, ","), "dataType argument should be provided");
		PerfTest perfTest = perfTestService.getOne(id);
		if (perfTest == null) {
			return toJsonHttpEntity(null);
		}
		return toJsonHttpEntity(getPerfGraphData(id, dataTypes, onlyTotal, imgWidth));
	}

	/**
	 * Get the monitor data of the target having the given IP.
	 *
	 * @param id       test Id
	 * @param targetIP targetIP
	 * @param imgWidth image width
	 * @return json message
	 */
	@RestAPI
	@RequestMapping("/api/{id}/monitor")
	public HttpEntity<String> getMonitorGraph(@PathVariable("id") long id,
											  @RequestParam("targetIP") String targetIP, @RequestParam int imgWidth) {
		return toJsonHttpEntity(getMonitorGraphData(id, targetIP, imgWidth));
	}

	/**
	 * Get the plugin monitor data of the target.
	 *
	 * @param id       test Id
	 * @param plugin   monitor plugin category
	 * @param kind     kind
	 * @param imgWidth image width
	 * @return json message
	 */
	@RestAPI
	@RequestMapping("/api/{id}/plugin/{plugin}")
	public HttpEntity<String> getPluginGraph(@PathVariable("id") long id,
											 @PathVariable("plugin") String plugin,
											 @RequestParam("kind") String kind, @RequestParam int imgWidth) {
		return toJsonHttpEntity(getReportPluginGraphData(id, plugin, kind, imgWidth));
	}

	private Map<String, Object> getReportPluginGraphData(long id, String plugin, String kind, int imgWidth) {
		int interval = perfTestService.getReportPluginGraphInterval(id, plugin, kind, imgWidth);
		Map<String, Object> pluginMonitorData = perfTestService.getReportPluginGraph(id, plugin, kind, interval);
		final PerfTest perfTest = perfTestService.getOne(id);
		int samplingInterval = 3;
		if (perfTest != null) {
			samplingInterval = perfTest.getSamplingInterval();
		}
		pluginMonitorData.put("interval", interval * samplingInterval);
		return pluginMonitorData;
	}


	/**
	 * Get the last perf test details in the form of json.
	 *
	 * @param user user
	 * @param page page
	 * @param size size of retrieved perf test
	 * @return json string
	 */
	@RestAPI
	@RequestMapping(value = {"/api/last", "/api", "/api/"}, method = RequestMethod.GET)
	public HttpEntity<String> getAll(User user, @RequestParam(value = "page", defaultValue = "0") int page,
									 @RequestParam(value = "size", defaultValue = "1") int size) {
		PageRequest pageRequest = new PageRequest(page, size, new Sort(Sort.Direction.DESC, "id"));
		Page<PerfTest> testList = perfTestService.getPagedAll(user, null, null, null, pageRequest);
		return toJsonHttpEntity(testList.getContent());
	}

	/**
	 * Get the perf test detail in the form of json.
	 *
	 * @param user user
	 * @param id   perftest id
	 * @return json message containing test info.
	 */
	@RestAPI
	@RequestMapping(value = "/api/{id}", method = RequestMethod.GET)
	public HttpEntity<String> getApiOne(User user, @PathVariable("id") Long id) {
		PerfTest test = checkNotNull(getOneWithPermissionCheck(user, id, false), "PerfTest %s does not exists", id);
		return toJsonHttpEntity(test);
	}

	/**
	 * Create the given perf test.
	 *
	 * @param user     user
	 * @param perfTest perf test
	 * @return json message containing test info.
	 */
	@RestAPI
	@RequestMapping(value = {"/api/", "/api"}, method = RequestMethod.POST)
	public HttpEntity<String> create(User user, PerfTest perfTest) {
		checkNull(perfTest.getId(), "id should be null");
		// Make the vuser count optional.
		if (perfTest.getVuserPerAgent() == null && perfTest.getThreads() != null && perfTest.getProcesses() != null) {
			perfTest.setVuserPerAgent(perfTest.getThreads() * perfTest.getProcesses());
		}
		validate(user, null, perfTest);
		PerfTest savePerfTest = perfTestService.save(user, perfTest);
		return toJsonHttpEntity(savePerfTest);
	}

	/**
	 * Delete the given perf test.
	 *
	 * @param user user
	 * @param id   perf test id
	 * @return json success message if succeeded
	 */
	@RestAPI
	@RequestMapping(value = "/api/{id}", method = RequestMethod.DELETE)
	public HttpEntity<String> delete(User user, @PathVariable("id") Long id) {
		PerfTest perfTest = getOneWithPermissionCheck(user, id, false);
		checkNotNull(perfTest, "no perftest for %s exits", id);
		perfTestService.delete(user, id);
		return successJsonHttpEntity();
	}


	/**
	 * Update the given perf test.
	 *
	 * @param user     user
	 * @param id       perf test id
	 * @param perfTest perf test configuration changes
	 * @return json message
	 */
	@RestAPI
	@RequestMapping(value = "/api/{id}", method = RequestMethod.PUT)
	public HttpEntity<String> update(User user, @PathVariable("id") Long id, PerfTest perfTest) {
		PerfTest existingPerfTest = getOneWithPermissionCheck(user, id, false);
		perfTest.setId(id);
		validate(user, existingPerfTest, perfTest);
		return toJsonHttpEntity(perfTestService.save(user, perfTest));
	}

	/**
	 * Stop the given perf test.
	 *
	 * @param user user
	 * @param id   perf test id
	 * @return json success message if succeeded
	 */
	@RestAPI
	@RequestMapping(value = "/api/stop/{id}", /**params = "action=stop",**/method = RequestMethod.PUT)
	public HttpEntity<String> stop(User user, @PathVariable("id") Long id) {
		perfTestService.stop(user, id);
		return successJsonHttpEntity();
	}


	/**
	 * Update the given perf test's status.
	 *
	 * @param user   user
	 * @param id     perf test id
	 * @param status Status to be moved to
	 * @return json message
	 */
	@RestAPI
	@RequestMapping(value = "/api/status/{id}", /**params = "action=status",**/method = RequestMethod.PUT)
	public HttpEntity<String> updateStatus(User user, @PathVariable("id") Long id, Status status) {
		PerfTest perfTest = getOneWithPermissionCheck(user, id, false);
		checkNotNull(perfTest, "no perftest for %s exits", id).setStatus(status);
		validate(user, null, perfTest);
		return toJsonHttpEntity(perfTestService.save(user, perfTest));
	}

	/**
	 * Clone and start the given perf test.
	 *
	 * @param user     user
	 * @param id       perf test id to be cloned
	 * @param perftest option to override while cloning.
	 * @return json string
	 */
	@SuppressWarnings("MVCPathVariableInspection")
	@RestAPI
	@RequestMapping(value = {"/api/{id}/clone_and_start", /* for backward compatibility */ "/api/{id}/cloneAndStart"})
	public HttpEntity<String> cloneAndStart(User user, @PathVariable("id") Long id, PerfTest perftest) {
		PerfTest test = getOneWithPermissionCheck(user, id, false);
		checkNotNull(test, "no perftest for %s exits", id);
		PerfTest newOne = test.cloneTo(new PerfTest());
		newOne.setStatus(Status.READY);
		if (perftest != null) {
			if (perftest.getScheduledTime() != null) {
				newOne.setScheduledTime(perftest.getScheduledTime());
			}
			if (perftest.getScriptRevision() != null) {
				newOne.setScriptRevision(perftest.getScriptRevision());
			}

			if (perftest.getAgentCount() != null) {
				newOne.setAgentCount(perftest.getAgentCount());
			}
		}
		if (newOne.getAgentCount() == null) {
			newOne.setAgentCount(0);
		}
		Map<String, MutableInt> agentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		MutableInt agentCountObj = agentCountMap.get(isClustered() ? test.getRegion() : Config.NONE_REGION);
		checkNotNull(agentCountObj, "test region should be within current region list");
		int agentMaxCount = agentCountObj.intValue();
		checkArgument(newOne.getAgentCount() != 0, "test agent should not be %s", agentMaxCount);
		checkArgument(newOne.getAgentCount() <= agentMaxCount, "test agent should be equal to or less than %s",
			agentMaxCount);
		PerfTest savePerfTest = perfTestService.save(user, newOne);
		CoreLogger.LOGGER.info("test {} is created through web api by {}", savePerfTest.getId(), user.getUserId());
		return toJsonHttpEntity(savePerfTest);
	}


//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Get the perf test lists.
	 * NEW
	 *
	 * @param user        user
	 * @param query       query string to search the perf test
	 * @param tag         tag
	 * @param queryFilter "F" means get only finished, "S" means get only scheduled tests.
	 * @param pageable    page
	 * @param model       modelMap
	 * @return perftest/list
	 */
	@RestAPI
	@ResponseBody
	@RequestMapping({"api/list", "api/", "api"})
	public HttpEntity getAllJson(User user, @RequestParam(required = false) String query,
								 @RequestParam(required = false) String tag, @RequestParam(required = false) String queryFilter,
								 @PageableDefault(page = 1, size = 10) Pageable pageable, ModelMap model) {
		pageable = new PageRequest(pageable.getPageNumber() - 1, pageable.getPageSize(),
			defaultIfNull(pageable.getSort(),
				new Sort(Sort.Direction.DESC, "id")));
		Page<PerfTest> tests = perfTestService.getPagedAll(user, query, tag, queryFilter, pageable);
		int page = pageable.getPageNumber();
		if (tests.getNumberOfElements() == 0) {
			pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), defaultIfNull(pageable.getSort(),
				new Sort(Sort.Direction.DESC, "id")));
			tests = perfTestService.getPagedAll(user, query, tag, queryFilter, pageable);
		}
		model.clear();
		annotateDateMarker(tests);
		model.addAttribute("tag", tag);
		model.addAttribute("availTags", tagService.getAllTagStrings(user, StringUtils.EMPTY));
		model.addAttribute("testList", tests.getContent());
		model.addAttribute("testUser", user.getUserName());
		model.addAttribute("totalPage", tests.getTotalPages());
		model.addAttribute("totalCount", tests.getTotalElements());
		model.addAttribute("queryFilter", queryFilter);
		model.addAttribute("query", query);
		putPageIntoModelMap(model, pageable);
//		return "perftest/list";
		return toJsonHttpEntity(model);
	}

	/**
	 * Get the running division in perftest configuration page.
	 * NEW  BUT not complete
	 *
	 * @param user  user
	 * @param model model
	 * @param id    test id
	 * @return perftest/running
	 */
	@RestAPI
	@ResponseBody
	@RequestMapping(value = "api/{id}/running_div")
	public ResponseEntity<PerfTest> getReportRunningDiv(User user, ModelMap model, @PathVariable long id) {
		PerfTest test = getOneWithPermissionCheck(user, id, false);
		model.addAttribute(PARAM_TEST, test);
//		return "perftest/running";
		return new ResponseEntity<PerfTest>(test, HttpStatus.OK);
	}

	/**
	 * Create a new test or cloneTo a current test.
	 *
	 * @param user     user
	 * @param perfTest {@link PerfTest}
	 * @param isClone  true if cloneTo
	 * @return redirect:/perftest/list
	 */
	@RestAPI
	@ResponseBody
	@RequestMapping(value = "api/new", method = RequestMethod.POST)
	public HttpEntity saveNew(User user, PerfTest perfTest,
							  @RequestParam(value = "isClone", required = false, defaultValue = "false") boolean isClone) {
		validate(user, null, perfTest);
		// Point to the head revision
		perfTest.setTestName(StringUtils.trimToEmpty(perfTest.getTestName()));
		perfTest.setScriptRevision(-1L);
		perfTest.prepare(isClone);
//		perfTest.setOwnerName(user.getUserName());
		perfTest = perfTestService.save(user, perfTest);
		PerfTest test = perfTestService.getOne(perfTest.getId());
//		if (perfTest.getStatus() == Status.SAVED || perfTest.getScheduledTime() != null) {
//			System.out.println( perfTest.getClass().getName() );
//			return new ResponseEntity(perfTest,HttpStatus.CREATED);
//		} else {
//			System.out.println( perfTest.getClass().getName() );
//			return new ResponseEntity(HttpStatus.CREATED);
//		}
		return toJsonHttpEntity(test);
	}

	/**
	 * Get the perf test creation form for quickStart.
	 *
	 * @param user       user
	 * @param urlString  URL string to be tested.
	 * @param scriptType scriptType
	 * @param model      model
	 * @return perftest/detail
	 */
	@RestAPI
	@ResponseBody
	@RequestMapping("api/quickstart")
	public HttpEntity quickStart(User user,
								 @RequestParam(value = "url", required = true) String urlString,
								 @RequestParam(value = "scriptType", required = true) String scriptType,
								 ModelMap model) {
		model.clear();
		URL url = checkValidURL(urlString);
		FileEntry newEntry = fileEntryService.prepareNewEntryForQuickTest(user, urlString,
			scriptHandlerFactory.getHandler(scriptType));
		model.addAttribute(PARAM_QUICK_SCRIPT, newEntry.getPath());
		model.addAttribute(PARAM_QUICK_SCRIPT_REVISION, newEntry.getRevision());
		PerfTest test = createPerfTestFromQuickStart(user, "Test for " + url.getHost(), url.getHost());
		model.addAttribute(PARAM_TEST, test);
		Map<String, MutableInt> agentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		model.addAttribute(PARAM_REGION_AGENT_COUNT_MAP, agentCountMap);
		model.addAttribute(PARAM_REGION_LIST, getRegions(agentCountMap));
		addDefaultAttributeOnModel(model);
		model.addAttribute(PARAM_PROCESS_THREAD_POLICY_SCRIPT, perfTestService.getProcessAndThreadPolicyScript());
		return toJsonHttpEntity(model);
//		return new ResponseEntity<PerfTest>(test, HttpStatus.CREATED);
	}

	@RequestMapping(value = "api/{id}/basic_report")
	public HttpEntity getApiBasicReport(User user, ModelMap model, @PathVariable long id, @RequestParam int imgWidth) {
		PerfTest test = getOneWithPermissionCheck(user, id, false);
		String runTime = test.getRuntimeStr();
		int interval = perfTestService.getReportDataInterval(id, "TPS", imgWidth);
		model.clear();
		model.addAttribute(PARAM_LOG_LIST, perfTestService.getLogFiles(id));
		model.addAttribute(PARAM_TEST_CHART_INTERVAL, interval * test.getSamplingInterval());
		model.addAttribute(PARAM_TEST, test);
		model.addAttribute("runTime", runTime);
		model.addAttribute(PARAM_TPS, perfTestService.getSingleReportDataAsJson(id, "TPS", interval));
		return toJsonHttpEntity(model);
	}

	/**
	 * Get the detailed perf test report.
	 *
	 * @param model model
	 * @param id    test id
	 * @return perftest/detail_report
	 */
	@SuppressWarnings("MVCPathVariableInspection")
	@RequestMapping(value = {"/api/{id}/detail_report", /** for backward compatibility */"/{id}/report"})
	public HttpEntity getDetailReport(ModelMap model, @PathVariable("id") long id) {
		model.clear();
		model.addAttribute("test", perfTestService.getOne(id));
		model.addAttribute("plugins", perfTestService.getAvailableReportPlugins(id));
		return toJsonHttpEntity(model);
	}

	/**
	 * start the given perf test.
	 *
	 * @param user user
	 * @param id   perf test id to be cloned
	 * @return json string
	 */
	@SuppressWarnings("MVCPathVariableInspection")
	@RestAPI
	@RequestMapping(value = {"/api/{id}/start"})
	public HttpEntity<String> Start(User user, @PathVariable("id") Long id) {
		PerfTest test = perfTestService.getOne(user, id);
		test.setStatus(Status.READY);
		checkNotNull(test, "no perftest for %s exits", id);
		if (test.getAgentCount() == null) {
			test.setAgentCount(0);
		}
		Map<String, MutableInt> agentCountMap = agentManagerService.getAvailableAgentCountMap(user);
		MutableInt agentCountObj = agentCountMap.get(isClustered() ? test.getRegion() : Config.NONE_REGION);
		checkNotNull(agentCountObj, "test region should be within current region list");
		int agentMaxCount = agentCountObj.intValue();
		checkArgument(test.getAgentCount() != 0, "test agent should not be %s", agentMaxCount);
		checkArgument(test.getAgentCount() <= agentMaxCount, "test agent should be equal to or less than %s",
			agentMaxCount);
		PerfTest savePerfTest = perfTestService.save(user, test);
		CoreLogger.LOGGER.info("test {} is created through web api by {}", test.getId(), user.getUserId());
		return toJsonHttpEntity(test);
	}


	/**
	 * 计算TPS图标数据
	 *
	 * @param sampleInterval nGrinder采集间隔
	 * @param thisDuration   展示数据的总时间区间，单位秒
	 * @param showInterval   展示数据的时间间隔，单位秒
	 * @param tpsStr         nGrinder采集数
	 * @param executeTime    测试执行时长
	 * @return tps数据
	 */
	private List<Map<String, Object>> getTps(int sampleInterval,
											 int thisDuration,
											 int showInterval,
											 String tpsStr,
											 long executeTime) {
		if (StringUtils.isEmpty(tpsStr)) {
			return Collections.emptyList();
		}
		thisDuration = Math.abs(thisDuration);

		// 获取tps原始数据
		JSONArray tpsValues = JSON.parseArray(tpsStr);
		if (tpsValues.isEmpty()) {
			return Collections.emptyList();
		}

		// 先把tps中为null的转换成前一个值
		for (int i = 0; i < tpsValues.size(); i++) {
			Object value = tpsValues.get(i);
			if (value == null) {
				tpsValues.set(i, i > 0 ? tpsValues.get(i - 1) : 0);
			}
		}

		// 根据nGrinder采集的间隔，把tps值封装成1秒钟一采集的数据，间隔中没有的值就使用紧邻的采集值
		JSONArray oneSecondIntervalTps = getOneSecondIntervalTpsArray(sampleInterval, tpsValues, executeTime);

		// 计算如果一秒钟采集一次，一共采集多少次
		int totalTimes = (int) executeTime / 1000;

		// 前端传入的查询数据时间区间，相当于要向当前时间之前查询采集多少次数据，查询时间区间-执行时间区间=需要填充的时间区间
		int neededSupplyTimes = thisDuration - totalTimes;

		// 如果拼接之后的一秒钟采集样本个数大于实际的执行时间，则计算取数开始位置
		List<Map<String, Object>> tpsInfos = new ArrayList<>();
		if (neededSupplyTimes > 0) {
			// 测试执行时间不足展示时间时，数据取值开始索引按照执行时间来计算
			int startIndex = oneSecondIntervalTps.size() - totalTimes;

			// 实际采样次数不够的部分使用默认时间和tps数据补足
			tpsInfos.addAll(buildTpsDefaultPartition(neededSupplyTimes, showInterval, thisDuration));

			// 补足默认数据之后，使用真实数据填充后面的数据
			tpsInfos.addAll(sampleTpsByInterval(showInterval, oneSecondIntervalTps, startIndex, totalTimes));
			return tpsInfos;
		}

		// 测试执行时间大于展示时间时，数据取值开始索引按照展示时间来计算
		int startIndex = oneSecondIntervalTps.size() - thisDuration;

		// 如果采样结果已经足够，则直接从采集结果中指定位置开始拿间隔频率的tps值返回,
		tpsInfos.addAll(sampleTpsByInterval(showInterval, oneSecondIntervalTps, startIndex, thisDuration));
		return tpsInfos;
	}

	/**
	 * 根据查询到的真是tps数据，构建需要返回格式的tps数据，按照频率{@see neededInterval}从指定位置{@see start}开始采集
	 *
	 * @param neededInterval       采集数据的频率
	 * @param oneSecondIntervalTps 1秒频率的原始数据
	 * @param start                数据采集开始的位置
	 * @param baseTime             数据采集开始时的基础时间，因为前面可能已经采集了部分，所以这里设置一个基础时间
	 * @return 固定是个的tps数据消息，包含了时间序列
	 */
	private List<Map<String, Object>> sampleTpsByInterval(int neededInterval,
														  JSONArray oneSecondIntervalTps,
														  int start,
														  int baseTime) {
		List<Map<String, Object>> tpsInfos = new ArrayList<>();
		int timeIndex = baseTime - 1;
		for (int i = start; i < oneSecondIntervalTps.size(); i++) {
			if (i % neededInterval != 0) {
				timeIndex--;
				continue;
			}
			Map<String, Object> thisTps = new HashMap<>();
			thisTps.put("tps", oneSecondIntervalTps.get(i));
			String format = "-%s:%s";
			if (timeIndex == 0) {
				thisTps.put("time", "00:00");
			} else {
				String time = String.format(Locale.ENGLISH, format, getMinuteString(timeIndex), getSecondsString(timeIndex));
				thisTps.put("time", time);
			}
			tpsInfos.add(thisTps);
			timeIndex--;
		}
		return tpsInfos;
	}

	/**
	 * 把原始tps数据按照采集频率封装成1秒钟采集频率的新tps数据
	 *
	 * @param taskTpsActualInterval tps实际采集频率
	 * @param tpsValues             tps原始数据
	 * @param executeTime           测试执行时间长度
	 * @return 封装好的采集频率为1秒的tps数据
	 */
	private JSONArray getOneSecondIntervalTpsArray(int taskTpsActualInterval, JSONArray tpsValues, long executeTime) {
		JSONArray oneSecondIntervalTps = new JSONArray();
		if (taskTpsActualInterval <= 0 || tpsValues == null || tpsValues.isEmpty()) {
			return oneSecondIntervalTps;
		}
		long seconds = executeTime / 1000;
		long totalSampleNumber = (long) tpsValues.size() * taskTpsActualInterval;

		// 如果按照每秒统计出来的结果与实际执行秒数匹配不上，就继续在最前面补充0
		if (totalSampleNumber < seconds) {
			for (int i = 0; i < seconds - totalSampleNumber; i++) {
				oneSecondIntervalTps.add(0);
			}
		}

		// 填充正常的采集数据值
		for (Object tpsValue : tpsValues) {
			for (int j = 0; j < taskTpsActualInterval; j++) {
				oneSecondIntervalTps.add(tpsValue);
			}
		}
		return oneSecondIntervalTps;
	}

	/**
	 * 如果实际采集的数据还不足支持需要的展示区间，则需要补充默认为0的tps数据
	 *
	 * @param needAddDuration 需要补足的采集区间
	 * @param interval        采集频率
	 * @param totalDuration   总共的时间区间
	 * @return 默认的格式tps格式数据
	 */
	private List<Map<String, Object>> buildTpsDefaultPartition(int needAddDuration, int interval, int totalDuration) {
		List<Map<String, Object>> tpsInfos = new ArrayList<>();
		for (int i = 0; i < Math.abs(needAddDuration); i++) {
			if (i % interval != 0) {
				continue;
			}
			Map<String, Object> thisTps = new HashMap<>();
			thisTps.put("tps", 0); // 缺省值补0
			String format = "-%s:%s";
			int showTimeTotalSeconds = totalDuration - 1 - i;
			thisTps.put("time", String.format(Locale.ENGLISH, format, getMinuteString(showTimeTotalSeconds), getSecondsString(showTimeTotalSeconds)));
			tpsInfos.add(thisTps);
		}
		return tpsInfos;
	}

	/**
	 * 根据时间封装tps数据格式中分钟部分字符串
	 *
	 * @param seconds 采集的时间，秒
	 * @return 计算之后的分钟值
	 */
	private String getMinuteString(int seconds) {
		int minute = seconds / 60;
		return minute < 10 ? "0" + minute : minute + "";
	}

	/**
	 * 根据时间封装tps数据格式中秒部分字符串
	 *
	 * @param seconds 采集的时间，秒
	 * @return 计算之后的秒值
	 */
	private String getSecondsString(int seconds) {
		int modSeconds = seconds % 60;
		return modSeconds < 10 ? "0" + modSeconds : modSeconds + "";
	}
}

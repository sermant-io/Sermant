package com.huawei.argus.scenario.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.argus.restcontroller.RestBaseController;
import org.ngrinder.model.Scenario;
import org.ngrinder.model.ScenarioPerfTest;
import com.huawei.argus.scenario.service.IScenarioPerfTestService;
import com.huawei.argus.scenario.service.IScenarioService;
import org.apache.commons.lang.math.NumberUtils;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.ngrinder.common.util.ObjectUtils.defaultIfNull;

@RestController
@RequestMapping("/rest/scenario")
public class ScenarioController extends RestBaseController {

	@Autowired
	IScenarioService scenarioService;

	@Autowired
	IScenarioPerfTestService scenarioPerfTestService;

	@RequestMapping("/save")
	public String create(User user, @RequestParam String scenarioInfos) {
		if (StringUtils.isEmpty(scenarioInfos)) {
			return returnError("压测场景信息缺失不完善");
		}
		Scenario scenario = JSONObject.parseObject(scenarioInfos, Scenario.class);
		scenario.setCreateBy(user);
		if (scenario.getId() != null) {
			// 刷新更新的字段
			Scenario oldScenario = scenarioService.getOne(scenario.getId());
			oldScenario.setAppName(scenario.getAppName());
			oldScenario.setDesc(scenario.getDesc());
			oldScenario.setLabel(scenario.getLabel());
			oldScenario.setScenarioName(scenario.getScenarioName());
			scenario = oldScenario;
		} else {
			scenario.setCreateTime(new Date());
		}
		scenario.setUpdateTime(new Date());
		scenario = scenarioService.save(scenario);
		return scenario.toString();
	}

	@RequestMapping("/list")
	public JSONObject getPagedAll(User user, @RequestParam(required = false) String query,
								  @RequestParam(required = false) String appNames,
								  @RequestParam(required = false) String createBy,
								  @RequestParam(required = false) String scenarioType,
								  @RequestParam(required = false) String scenarioName,
								  @RequestParam(required = false) String pages) {
		Pageable pageable = getPageable(pages);
		pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(),
			defaultIfNull(pageable.getSort(),
				new Sort(Sort.Direction.DESC, "id")));
		Page<Scenario> scenarios = scenarioService.getPagedAll(user, query, appNames, createBy, scenarioType,scenarioName, pageable);
		if (scenarios.getNumberOfElements() == 0) {
			pageable = new PageRequest(0, pageable.getPageSize(), defaultIfNull(pageable.getSort(),
				new Sort(Sort.Direction.DESC, "id")));
			scenarios = scenarioService.getPagedAll(user, query, appNames, createBy, scenarioType,scenarioName, pageable);
		}
		JSONObject modelInfos = new JSONObject();
		modelInfos.put("scenarioListPage", pageToJson(scenarios));
		putPageIntoModelMap(modelInfos, pageable);
		return modelInfos;
	}

	@RequestMapping("/allList")
	public JSONObject getAll(User user) {
		List<Scenario> content = scenarioService.findAll();
		JSONArray array = listToJsonArray(Arrays.asList(content.toArray()));
		JSONObject modelInfos = new JSONObject();
		modelInfos.put("content", array);
		return modelInfos;
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public String delete(User user, @RequestParam("ids") String ids) {
		for (String idStr : org.apache.commons.lang.StringUtils.split(ids, ",")) {
			if (StringUtils.isEmpty(idStr)) {
				continue;
			}
			scenarioService.delete(user, NumberUtils.toLong(idStr, 0));
			// 删除场景与压测任务的关系
			scenarioPerfTestService.deleteByOneId(user, null, null, Long.parseLong(idStr));
		}
		return returnSuccess();
	}

	/**
	 * 保存压测场景与压测任务关系
	 * @param scenarioPerfTestInfos
	 * @return
	 */
	@RequestMapping(value = "/saveScenarioPerfTest", method = RequestMethod.POST)
	public String saveScenarioPerfTest(@RequestParam String scenarioPerfTestInfos){
		ScenarioPerfTest scenarioPerfTest = JSONObject.parseObject(scenarioPerfTestInfos,ScenarioPerfTest.class);
		scenarioPerfTestService.save(scenarioPerfTest);
		return returnSuccess();
	}

	/**
	 * 查询压测任务对应的场景信息
	 * @param user
	 * @param perfTestId
	 * @return
	 */
	@RequestMapping(value = "/getAllByPerfTestId", method = RequestMethod.POST)
	public JSONObject getAllByPerfTestId(User user, @RequestParam Long perfTestId){
		List<ScenarioPerfTest> allByID = scenarioPerfTestService.getAllByID(user, perfTestId, null);
		if (allByID != null && !allByID.isEmpty()) {
			Long scenarioId = allByID.get(0).getScenarioId();
			Scenario scenario = scenarioService.getOne(scenarioId);
			return modelStrToJson(scenario.toString());
		}
		return null;
	}

}

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

package com.huawei.argus.perftest.service.impl;

import com.huawei.argus.perftest.repository.PerfTestTaskRepository;
import com.huawei.argus.perftest.service.IPerfTestTaskService;
import com.huawei.argus.serializer.PerfSceneType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.ngrinder.infra.config.Config;
import org.ngrinder.model.*;
import org.ngrinder.perftest.repository.PerfTestRepository;
import org.ngrinder.perftest.service.ConsoleManager;
import org.ngrinder.perftest.service.TagService;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.service.FileEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;

import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.Preconditions.checkNotNull;
import static org.ngrinder.model.Status.READY;
import static org.ngrinder.perftest.repository.PerfTestSpecification.*;

@Service
public class PerfTestTaskService implements IPerfTestTaskService {

	private static final Logger logger = LoggerFactory.getLogger(PerfTestTaskService.class);

	@Autowired
	private PerfTestTaskRepository perfTestTaskRepository;
	@Autowired
	private Config config;
	@Autowired
	private ConsoleManager consoleManager;
	@Autowired
	private PerfTestRepository perfTestRepository;

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private TagService tagService;


	@Override
	public Page<PerfTest> getPagedAll(User user, String query, String tag, String queryFilter, Pageable pageable) {
		Specifications<PerfTest> spec = Specifications.where(idEmptyPredicate());
		// User can see only his own test
		if (user.getRole().equals(Role.USER)) {
			spec = spec.and(createdBy(user));
		}

		if (StringUtils.isNotBlank(tag)) {
			spec = spec.and(hasTag(tag));
		}
		if ("F".equals(queryFilter)) {
			spec = spec.and(statusSetEqual(Status.FINISHED));
		} else if ("R".equals(queryFilter)) {
			spec = spec.and(statusSetEqual(Status.TESTING));
		} else if ("S".equals(queryFilter)) {
			spec = spec.and(statusSetEqual(Status.READY));
			spec = spec.and(scheduledTimeNotEmptyPredicate());
		}
		if (StringUtils.isNotBlank(query)) {
			spec = spec.and(likeTestNameOrDescription(query));
		}
		return perfTestTaskRepository.findAll(spec, pageable);
	}

	@Override
	public void deletePerfTests(User user, String ids) {

		for (String idStr : StringUtils.split(ids, ",")) {
			delete(user, NumberUtils.toLong(idStr, 0));
		}
	}

	private void delete(User user, long id) {
		PerfTest perfTest = getOne(id);
		if(perfTest==null)return;
		// 判断是否为当前用户的请求操作
		if (!hasPermission(perfTest, user, Permission.DELETE_TEST_OF_OTHER)) {
			return;
		}
		SortedSet<Tag> tags = perfTest.getTags();
		if (tags != null) {
			tags.clear();
		}
		/**
		 * 删除monitoring;perfScene
		 */



		perfTestTaskRepository.save(perfTest);
		perfTestTaskRepository.delete(perfTest);
		deletePerfTestDirectory(perfTest);
	}

	@Override
	public PerfTest getOne(Long testId) {
		return perfTestTaskRepository.findOne(testId);
	}

	@Override
	public File getPerfTestDirectory(PerfTest perfTest) {
		return config.getHome().getPerfTestDirectory(perfTest);
	}

	@Transactional
	@Override
	public PerfTest getOneWithTag(Long testId) {
		PerfTest findOne = perfTestTaskRepository.findOne(testId);
		if (findOne != null) {
			Hibernate.initialize(findOne.getTags());
		}
		return findOne;
	}

	@Override
	public String getProcessAndThreadPolicyScript() {
		return config.getProcessAndThreadPolicyScript();
	}

	@Override
	@Transactional
	public PerfTest save(User user, PerfTest perfTest) {
		// 从场景中获取脚本路径
		PerfScene perfScene = perfTest.getPerfScene();
		if (perfScene.getType().equals(PerfSceneType.SCRIPT) || perfScene.getType().equals(PerfSceneType.TRAFFIC))
			perfTest.setScriptName(perfTest.getPerfScene().getScriptPath());

		attachFileRevision(user, perfTest);
		attachTags(user, perfTest, perfTest.getTagString());
		if(perfTest.getStatus().equals(READY)){
			perfTest.clearMessages();
			deletePerfTestDirectory(perfTest);
		}

		return save(perfTest);
	}

	@Override
	public PerfTest getPerfTestTask(User user, Long id, boolean withTag) {
		PerfTest perfTest = withTag ? getOneWithTag(id) : getOne(id);
		String perfTestReportId = perfTestTaskRepository.getReportIdByTestId(id);
		if(StringUtils.isNotBlank(perfTestReportId)){
			perfTest.setPerfTestReportId(Long.valueOf(perfTestReportId));
		}
		if (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.SUPER_USER)) {
			return perfTest;
		}
		if (perfTest != null && !user.equals(perfTest.getCreatedUser())) {
			throw processException("User " + user.getUserId() + " has no right on PerfTest " + id);
		}
		return perfTest;
	}

	@Override
	public PerfTest startPerfTest(User user, PerfTest perfTest) {

		if (perfTest.getStatus() == Status.SAVED || perfTest.getStatus() == Status.FINISHED){
			perfTest.clearMessages();
			perfTest = initStartPerfTest(perfTest);
			deletePerfTestDirectory(perfTest);
			perfTest.setStopRequest(Boolean.FALSE);
			perfTest.setStatus(READY);
			perfTest = perfTestTaskRepository.saveAndFlush(perfTest);
			return perfTest;
		}

		if(perfTest.getStatus() == Status.STOP_BY_ERROR || perfTest.getStatus() == Status.UNKNOWN || perfTest.getStatus() == Status.CANCELED){
			perfTest.clearMessages();
			perfTest = initStartPerfTest(perfTest);
			deletePerfTestDirectory(perfTest);
			perfTest.setStopRequest(Boolean.FALSE);
			perfTest.setStatus(READY);
			perfTest = perfTestTaskRepository.saveAndFlush(perfTest);
			return perfTest;
		}
		return null;
	}

	@Override
	public void stopPerfTests(User user, String ids) {
		for (String idStr : StringUtils.split(ids, ",")) {
			stop(user, NumberUtils.toLong(idStr, 0));
		}
	}

	@Override
	public List<String> getSelectAgentNameList(PerfTest perfTest) {
		String agentIds = perfTest.getAgentIds();
		String[] split = agentIds.split(",");
		List<String> ids = Arrays.asList(split);
		List<String> selectAgentNameList = perfTestTaskRepository.getSelectAgentNameList(ids);

		logger.info("selectAgentNameList:{}",selectAgentNameList);
		return selectAgentNameList;
	}

	@Override
	public List<String> getLogFiles(Long testId) {
		File logFileDirectory = getLogFileDirectory(String.valueOf(testId));
		if (!logFileDirectory.exists() || !logFileDirectory.isDirectory()) {
			return Collections.emptyList();
		}
		return Arrays.asList(logFileDirectory.list());
	}

	public File getLogFileDirectory(String testId) {
		return config.getHome().getPerfTestLogDirectory(testId);
	}
	//	@Override
	@Transactional
	public void stop(User user, Long id) {
		PerfTest perfTest = getOne(id);
		// If it's not requested by user who started job. It's wrong request.
		if (!hasPermission(perfTest, user, Permission.STOP_TEST_OF_OTHER)) {
			return;
		}
		// If it's not stoppable status.. It's wrong request.
		if (!perfTest.getStatus().isStoppable()) {
			return;
		}
		// Just mark cancel on console
		// This will be not be effective on cluster mode.
		consoleManager.getConsoleUsingPort(perfTest.getPort()).cancel();
		perfTest.setStopRequest(true);
		perfTestTaskRepository.save(perfTest);
	}

	private PerfTest initStartPerfTest(PerfTest perfTest){
		perfTest.setMeanTestTime(null);
		perfTest.setPeakTps(null);
		perfTest.setTps(null);
		perfTest.setTests(null);
		perfTest.setTestErrorCause(null);
		return perfTest;
	}

	public boolean hasPermission(PerfTest perfTest, User user, Permission type) {
		return perfTest != null && (user.getRole().hasPermission(type) || user.equals(perfTest.getCreatedUser()));
	}

	private void deletePerfTestDirectory(PerfTest perfTest) {
		FileUtils.deleteQuietly(getPerfTestDirectory(perfTest));
	}
	private void attachFileRevision(User user, PerfTest perfTest) {
		if (perfTest.getStatus() == Status.READY) {
			FileEntry scriptEntry = fileEntryService.getOne(user, perfTest.getScriptName());
			long revision = scriptEntry != null ? scriptEntry.getRevision() : -1;
			perfTest.setScriptRevision(revision);
		}
	}

	private void attachTags(User user, PerfTest perfTest, String tagString) {
		SortedSet<Tag> tags = tagService.addTags(user,
			org.apache.commons.lang.StringUtils.split(org.apache.commons.lang.StringUtils.trimToEmpty(tagString), ","));
		perfTest.setTags(tags);
		perfTest.setTagString(buildTagString(tags));
	}
	private String buildTagString(Set<Tag> tags) {
		List<String> tagStringResult = new ArrayList<String>();
		for (Tag each : tags) {
			tagStringResult.add(each.getTagValue());
		}
		return org.apache.commons.lang.StringUtils.join(tagStringResult, ",");
	}
	private PerfTest save(PerfTest perfTest) {
		checkNotNull(perfTest);
		// Merge if necessary
		if (perfTest.exist()) {
			PerfTest existingPerfTest = perfTestTaskRepository.findOne(perfTest.getId());
			perfTest = existingPerfTest.merge(perfTest);
		} else {
			perfTest.clearMessages();
		}
		if(perfTest.getStopRequest()!=null && perfTest.getStopRequest()){
			perfTest.setStopRequest(Boolean.FALSE);
		}

		return perfTestTaskRepository.saveAndFlush(perfTest);
	}

}

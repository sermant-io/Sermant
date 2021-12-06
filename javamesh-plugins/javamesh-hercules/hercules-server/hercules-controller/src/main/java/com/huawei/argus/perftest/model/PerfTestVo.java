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

package com.huawei.argus.perftest.model;

import com.huawei.argus.serializer.PerfSceneType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.ngrinder.common.util.DateUtils;
import org.ngrinder.common.util.PathUtils;
import org.ngrinder.model.RampUp;
import org.ngrinder.model.Status;
import org.ngrinder.model.Tag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * Performance Test Entity.
 */

@SuppressWarnings({"JpaDataSourceORMInspection", "UnusedDeclaration", "JpaAttributeTypeInspection"})
public class PerfTestVo implements Serializable {



	private Long id;
//	private Date createdDate;

//	private Date lastModifiedDate;

	private String testName;
	private String tagString;
	private String description;
	private Status status;
	private Integer ignoreSampleCount;

//	private Date scheduledTime;

	private Long perfSceneId;
//	private Date startTime;

//	private Date finishTime;

	private String targetHosts;


	private Boolean useRampUp;

	public RampUp getRampUpType() {
		return rampUpType;
	}

	public void setRampUpType(RampUp rampUpType) {
		this.rampUpType = rampUpType;
	}


	private RampUp rampUpType;



	private String threshold;


	private String scriptName;


	private Long duration;


	private Integer runCount;

	private Integer agentCount;

	private Integer vuserPerAgent;

	private Integer processes;

	private Integer rampUpInitCount;

	private Integer rampUpInitSleepTime;

	private Integer rampUpStep;

	private Integer rampUpIncrementInterval;

	private Integer threads;
	private String distributionPath;

	private Long tests;

	private String testComment;


	private Long scriptRevision;


	private String region;

	private Boolean safeDistribution;


	private SortedSet<Tag> tags;


	private String runningSample;


	private String agentState;


	private Integer samplingInterval;


	private String param;


//	private MonitoringConfig monitoringConfig;

//	private String agentIds;

	private int[] agentIds;

	private PerfSceneType sceneType;


	public PerfSceneType getSceneType() {
		return sceneType;
	}

	public void setSceneType(PerfSceneType sceneType) {
		this.sceneType = sceneType;
	}

	public long getTotalRunCount() {
		return getAgentCount() * getThreads() * getProcesses() * (long) getRunCount();
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public Integer getRunCount() {
		return runCount;
	}

	public void setRunCount(Integer runCount) {
		this.runCount = runCount;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	public Integer getIgnoreSampleCount() {
		return ignoreSampleCount;
	}

	public void setIgnoreSampleCount(Integer ignoreSampleCount) {
		this.ignoreSampleCount = ignoreSampleCount;
	}

	public String getScriptNameInShort() {
		return PathUtils.getShortPath(scriptName);
	}



	public void setDescription(String description) {
		this.description = description;
	}

	public String getTargetHosts() {
		return targetHosts;
	}

	/**
	 * Get ip address of target hosts. if target hosts 'a.com:1.1.1.1' add ip: '1.1.1.1' if target
	 * hosts ':1.1.1.1' add ip: '1.1.1.1' if target hosts '1.1.1.1' add ip: '1.1.1.1'
	 * if www.test.com:0:0:0:0:0:ffff:3d87:a969 add ip: '0:0:0:0:0:ffff:3d87:a969'
	 *
	 * @return host ip list
	 */
	public List<String> getTargetHostIP() {
		List<String> targetIPList = new ArrayList<String>();
		String[] hostsList = StringUtils.split(StringUtils.trimToEmpty(targetHosts), ",");
		for (String hosts : hostsList) {
			String[] addresses = StringUtils.split(hosts, ":");
			if (addresses.length <= 2) {
				targetIPList.add(addresses[addresses.length - 1]);
			} else {
				targetIPList.add(hosts.substring(hosts.indexOf(":") + 1, hosts.length()));
			}
		}
		return targetIPList;
	}

	public void setTargetHosts(String theTarget) {
		this.targetHosts = theTarget;
	}

	public String getThreshold() {
		return threshold;
	}

	public Boolean isThresholdDuration() {
		return "D".equals(getThreshold());
	}

	public Boolean isThresholdRunCount() {
		return "R".equals(getThreshold());
	}


	public void setThreshold(String threshold) {
		this.threshold = threshold;
	}


	public Status getStatus() {
		return status;
	}


	public void setStatus(Status status) {
		this.status = status;
	}

	public Integer getAgentCount() {
		return agentCount;
	}


	public void setAgentCount(Integer agentCount) {
		this.agentCount = agentCount;
	}

	public Integer getVuserPerAgent() {
		return vuserPerAgent;
	}


	public void setVuserPerAgent(Integer vuserPerAgent) {
		this.vuserPerAgent = vuserPerAgent;
	}

	public Integer getProcesses() {
		return processes;
	}


	public void setProcesses(Integer processes) {
		this.processes = processes;
	}

	public Integer getRampUpInitCount() {
		return rampUpInitCount;
	}

	public void setRampUpInitCount(Integer initProcesses) {
		this.rampUpInitCount = initProcesses;
	}

	public Integer getRampUpInitSleepTime() {
		return rampUpInitSleepTime;
	}


	public void setRampUpInitSleepTime(Integer initSleepTime) {
		this.rampUpInitSleepTime = initSleepTime;
	}

	public Integer getRampUpStep() {
		return rampUpStep;
	}


	public void setRampUpStep(Integer processIncrement) {
		this.rampUpStep = processIncrement;
	}

	public Integer getRampUpIncrementInterval() {
		return rampUpIncrementInterval;
	}


	public void setRampUpIncrementInterval(Integer processIncrementInterval) {
		this.rampUpIncrementInterval = processIncrementInterval;
	}

	public Integer getThreads() {
		return threads;
	}


	public void setThreads(Integer threads) {
		this.threads = threads;
	}



	public String getDistributionPath() {
		return distributionPath;
	}

	public void setDistributionPath(String distributionPath) {
		this.distributionPath = distributionPath;
	}

	/**
	 * Get Duration time in HH:MM:SS style.
	 *
	 * @return formatted duration string
	 */
	public String getDurationStr() {
		return DateUtils.ms2Time(this.duration);
	}


	/**
	 * Get Running time in HH:MM:SS style.
	 *
	 * @return formatted runtime string
	 */

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toStringExclude(this, "tags");
	}


	public Long getScriptRevision() {
		return scriptRevision;
	}


	public void setScriptRevision(Long scriptRevision) {
		this.scriptRevision = scriptRevision;
	}



	public Boolean getUseRampUp() {
		return useRampUp;
	}


	public void setUseRampUp(Boolean useRampUp) {
		this.useRampUp = useRampUp;
	}



	public String getTagString() {
		return tagString;
	}


	public void setTagString(String tagString) {
		this.tagString = tagString;
	}

	public SortedSet<Tag> getTags() {
		return tags;
	}

	public void setTags(SortedSet<Tag> tags) {
		this.tags = tags;
	}

	public String getRegion() {
		return region;
	}


	public void setRegion(String region) {
		this.region = region;
	}

	public Boolean getSafeDistribution() {
		return safeDistribution == null ? Boolean.FALSE : safeDistribution;
	}


	public void setSafeDistribution(Boolean safeDistribution) {
		this.safeDistribution = safeDistribution;
	}

	public String getRunningSample() {
		return runningSample;
	}


	public void setRunningSample(String runningSample) {
		this.runningSample = runningSample;
	}

	public String getAgentState() {
		return agentState;
	}

	public void setAgentState(String agentStatus) {
		this.agentState = agentStatus;
	}

	public Integer getSamplingInterval() {
		return samplingInterval;
	}


	public void setSamplingInterval(Integer samplingInterval) {
		this.samplingInterval = samplingInterval;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}


	private boolean nmonAll;


	private boolean jvmThr;


	private boolean jvmCpu;

	private boolean jvmMem;

	private boolean jvmCl;


	private boolean jvmGc;

	public boolean isNmonAll() {
		return nmonAll;
	}

	public void setNmonAll(boolean nmonAll) {
		this.nmonAll = nmonAll;
	}

	public boolean isJvmThr() {
		return jvmThr;
	}

	public void setJvmThr(boolean jvmThr) {
		this.jvmThr = jvmThr;
	}

	public boolean isJvmCpu() {
		return jvmCpu;
	}

	public void setJvmCpu(boolean jvmCpu) {
		this.jvmCpu = jvmCpu;
	}

	public boolean isJvmMem() {
		return jvmMem;
	}

	public void setJvmMem(boolean jvmMem) {
		this.jvmMem = jvmMem;
	}

	public boolean isJvmCl() {
		return jvmCl;
	}

	public void setJvmCl(boolean jvmCl) {
		this.jvmCl = jvmCl;
	}

	public boolean isJvmGc() {
		return jvmGc;
	}

	public void setJvmGc(boolean jvmGc) {
		this.jvmGc = jvmGc;
	}

	public boolean isJvmMp() {
		return jvmMp;
	}

	public void setJvmMp(boolean jvmMp) {
		this.jvmMp = jvmMp;
	}

	private boolean jvmMp;

//	public String getAgentIds() {
//		return agentIds;
//	}
//
//	public void setAgentIds(String agentIds) {
//		this.agentIds = agentIds;
//	}


	public int[] getAgentIds() {
		return agentIds;
	}

	public void setAgentIds(int[] agentIds) {
		this.agentIds = agentIds;
	}

//	public MonitoringConfig getMonitoringConfig() {
//		return monitoringConfig;
//	}
//
//	public void setMonitoringConfig(MonitoringConfig monitoringConfig) {
//		this.monitoringConfig = monitoringConfig;
//	}
	public Long getTests() {
		return tests;
	}

	public void setTests(Long tests) {
		this.tests = tests;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}


	public String getTestComment() {
		return testComment;
	}

	public void setTestComment(String testComment) {
		this.testComment = testComment;
	}

	public Long getPerfSceneId() {
		return perfSceneId;
	}

	public void setPerfSceneId(Long perfSceneId) {
		this.perfSceneId = perfSceneId;
	}
}


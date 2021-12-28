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

package com.huawei.argus.perftest.util;

import com.huawei.argus.perftest.model.PerfTestVo;
import org.ngrinder.model.PerfTest;

/**
 * @author lWX716491
 * @date 2019/05/12 10:33
 */
public class PerfTestUtil {



	public static PerfTestVo perfTestToVo(PerfTest perfTest){
		PerfTestVo perfTestVo = new PerfTestVo();
		perfTestVo.setTestName(perfTest.getTestName());
		perfTestVo.setTagString(perfTest.getTagString());		//		"tagString": "",
		perfTestVo.setDescription(perfTest.getDescription());					//			"description": "",
        perfTestVo.setStatus(perfTest.getStatus());							//			"status": "FINISHED",
		perfTestVo.setIgnoreSampleCount(perfTest.getIgnoreSampleCount());				//			"ignoreSampleCount": 0,
		perfTestVo.setTargetHosts(perfTest.getTargetHosts());					//			"targetHosts": "DFAS:110.121",
		perfTestVo.setUseRampUp(perfTest.getUseRampUp());					//			"useRampUp": false,
		perfTestVo.setRampUpType(perfTest.getRampUpType());					//			"rampUpType": "THREAD",
		perfTestVo.setThreshold(perfTest.getThreshold());					//			"threshold": "D",
		perfTestVo.setScriptName(perfTest.getScriptName());					//			"scriptName": "scarb-argus.py",
		perfTestVo.setDuration(perfTest.getDuration());					//			"duration": 60000,
		perfTestVo.setRunCount(perfTest.getRunCount());					//			"runCount": 0,
		perfTestVo.setAgentCount(perfTest.getAgentCount());					//			"agentCount": 1,
		perfTestVo.setVuserPerAgent(perfTest.getVuserPerAgent());					//			"vuserPerAgent": 1,
		perfTestVo.setProcesses(perfTest.getProcesses());					//			"processes": 1,
		perfTestVo.setRampUpInitCount(perfTest.getRampUpInitCount());					//			"rampUpInitCount": 0,
		perfTestVo.setRampUpInitSleepTime(perfTest.getRampUpInitSleepTime());					//			"rampUpInitSleepTime": 0,
		perfTestVo.setRampUpStep(perfTest.getRampUpStep());					//			"rampUpStep": 1,
		perfTestVo.setRampUpIncrementInterval(perfTest.getRampUpIncrementInterval());					//			"rampUpIncrementInterval": 1000,
		perfTestVo.setThreads(perfTest.getThreads());					//			"threads": 1,
		perfTestVo.setTests(perfTest.getTests()==null ? 0 : perfTest.getTests());					//			"tests": 245,
        perfTestVo.setTestComment(perfTest.getTestComment());							//			"testComment": "",
		perfTestVo.setScriptRevision(perfTest.getScriptRevision());					//			"scriptRevision": 677,
		perfTestVo.setRegion(perfTest.getRegion());					//			"region": "NONE",
		perfTestVo.setSamplingInterval(perfTest.getSamplingInterval());					//			"samplingInterval": 1,
		perfTestVo.setParam(perfTest.getParam());					//			"param": "0",
		perfTestVo.setId(perfTest.getId());					//			"id": 57
		perfTestVo.setSafeDistribution(perfTest.getSafeDistribution());

		if (perfTest.getPerfScene() != null){
			perfTestVo.setPerfSceneId(perfTest.getPerfScene().getId());
			perfTestVo.setSceneType(perfTest.getPerfScene().getType());
		}

		String agentIds = perfTest.getAgentIds();
		int [] agentId = null;
		if (agentIds != null && agentIds.length() > 0) {
			String[] split = agentIds.split(",");
			agentId = new int[split.length];
			for (int i = 0; i < split.length; i++) {
				agentId[i] = Integer.parseInt(split[i]);

			}
			perfTestVo.setAgentIds(agentId);                    //			"agentIds": "1",
		}
		return perfTestVo;
	}
}

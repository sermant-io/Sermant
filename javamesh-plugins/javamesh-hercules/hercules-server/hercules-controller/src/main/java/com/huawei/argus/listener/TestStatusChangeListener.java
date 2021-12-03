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

package com.huawei.argus.listener;

import com.alibaba.fastjson.JSONObject;
import org.ngrinder.model.PerfTest;
import org.ngrinder.perftest.service.PerfTestRunnable;
import org.ngrinder.service.IPerfTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述：状态变更报告器
 *
 * @author z30009938
 * @since 2021-11-03
 */
@Component
@PropertySource("classpath:websocket.properties")
public class TestStatusChangeListener implements ITestLifeCycleListener {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TestStatusChangeListener.class);

	@Autowired
	private RestTemplate restTemplate;

	@Value("${task.status.update.notify.uri}")
	private String taskStatusChangeNotifyUri;

	@Value("${websocket.notify.host}")
	private String taskStatusChangeNotifyHost;

	/**
	 * 消息前缀
	 */
	private static final String MESSAGE_PREFIX = "/task/";

	@PostConstruct
	public void init() {
		PerfTestRunnable.allTestLifeCycleListeners.add(this);
	}

	@Override
	public void start(PerfTest perfTest, IPerfTestService perfTestService, String version) {
		sendMessage(MESSAGE_PREFIX + perfTest.getId());
	}

	@Override
	public void finish(PerfTest perfTest, String stopReason, IPerfTestService perfTestService, String version) {
		sendMessage(MESSAGE_PREFIX + perfTest.getId());
	}

	/**
	 * 发送任务状态改变通知到前端服务
	 *
	 * @param message 任务状态改变的消息
	 */
	public void sendMessage(String message) {
		if (StringUtils.isEmpty(taskStatusChangeNotifyUri)) {
			LOGGER.error("The property task.status.update.notify.uri must have value.");
			return;
		}
		if (StringUtils.isEmpty(taskStatusChangeNotifyHost)) {
			LOGGER.error("The property task.status.update.notify.host must have value.");
			return;
		}
		String[] hosts = taskStatusChangeNotifyHost.split(",");
		List<String> urls = new ArrayList<>();
		for (String host : hosts) {
			if (StringUtils.isEmpty(host)) {
				LOGGER.warn("Found invalid host when send task status, invalid host:{}", host);
				continue;
			}
			urls.add("http://" + host + taskStatusChangeNotifyUri);
		}
		Map<String, Object> data = new HashMap<>();
		data.put("message", message);
		for (String url : urls) {
			try {
				JSONObject response = restTemplate.postForObject(url, data, JSONObject.class);
				if (!response.containsKey("result") || !response.getString("result").equals("success")) {
					LOGGER.warn("Send task status fail, url:{}", url);
				}
			} catch (Exception exception) {
				LOGGER.error("Send task status change info fail, exception:{}", exception.getMessage());
			}
		}
	}
}

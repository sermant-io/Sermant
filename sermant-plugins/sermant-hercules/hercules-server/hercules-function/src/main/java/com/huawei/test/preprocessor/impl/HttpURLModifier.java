/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.test.preprocessor.impl;

import com.huawei.test.preprocessor.URLModifier;
import com.huawei.test.preprocessor.config.HttpURLModifierConfig;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能描述：HTTP URL session id 配置修改
 *
 * @author zl
 * @since 2021-12-09
 */
public class HttpURLModifier extends URLModifier<HttpURLModifierConfig> {
	/**
	 * 全局缓存sessionId,根据线程名称来保存
	 */
	private static final Map<String, String> cacheSessionIds = new ConcurrentHashMap<>();

	@Override
	public String modifyUrl(String url, String sessionId, HttpURLModifierConfig config) {
		if (StringUtils.isEmpty(url)) {
			return url;
		}
		String threadName = Thread.currentThread().getName();
		if (StringUtils.isEmpty(sessionId) && !cacheSessionIds.containsKey(threadName)) {
			return url;
		}
		cacheSessionIds.put(threadName, sessionId);
		if (config == null) {
			return url;
		}
		String[] urlPartitions = url.split("\\?");
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(urlPartitions[0]);
		if (config.useSemicolonSeparator()) {
			urlBuilder.append(";").append(config.getSessionArgumentName()).append("=").append(sessionId);
		}
		if(urlPartitions.length > 1) {
			urlBuilder.append("?").append(urlPartitions[1]);
		}
		return urlBuilder.toString();
	}
}

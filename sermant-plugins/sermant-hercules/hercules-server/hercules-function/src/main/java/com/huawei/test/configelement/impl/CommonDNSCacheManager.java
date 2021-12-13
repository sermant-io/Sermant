/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.test.configelement.impl;

import com.huawei.test.configelement.DNSCacheManager;
import com.huawei.test.configelement.config.DNSCacheConfig;

import java.util.List;
import java.util.Map;

/**
 * 功能描述：DNS 域名管理逻辑实现
 *
 * @author zl
 * @since 2021-12-09
 */
public class CommonDNSCacheManager extends DNSCacheManager {
	@Override
	public String getIp(String hostName) {
		return null;
	}

	@Override
	public List<Map<String, String>> getHosts() {
		return null;
	}

	@Override
	public void initConfig(DNSCacheConfig config) {

	}
}

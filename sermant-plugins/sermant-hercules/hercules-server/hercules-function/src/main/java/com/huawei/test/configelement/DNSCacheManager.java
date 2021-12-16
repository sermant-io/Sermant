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

package com.huawei.test.configelement;

import com.huawei.test.configelement.config.DNSCacheConfig;

import java.util.List;
import java.util.Map;

/**
 * 功能描述：DNS管理器
 *
 * @author zl
 * @since 2021-12-09
 */
public abstract class DNSCacheManager extends ConfigElement<DNSCacheConfig> {
	/**
	 * 获取指定hostName对应的ip
	 *
	 * @param hostName 注解名称
	 * @return 主机名称对应的ip
	 */
	public abstract String getIp(String hostName);

	/**
	 * 获取所有hosts信息
	 *
	 * @return 通过DNS解析出的所有host信息
	 */
	public abstract List<Map<String,String>> getHosts();
}

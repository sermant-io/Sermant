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

package com.huawei.test.configelement.config;

import com.huawei.test.configelement.enums.DNSResolverMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 功能描述：DNS服务器相关配置
 *
 * @author zl
 * @since 2021-12-09
 */
public class DNSCacheConfig {
	/**
	 * 是否每迭代都刷新dns中cache缓存
	 */
	private final boolean refreshEveryTime;

	/**
	 * dns服务器使用模式
	 */
	private final DNSResolverMode dnsResolverMode;

	/**
	 * 如果是自定义模式，这里保存自定义域名解析服务器的地址
	 */
	private final List<String> customResolvers;

	/**
	 * 如果是自定义模式，这里保存自定义hosts信息
	 */
	private final List<Map<String, String>> customHosts;

	private DNSCacheConfig(Builder builder) {
		this.refreshEveryTime = builder.refreshEveryTime;
		this.dnsResolverMode = builder.dnsResolverMode;
		this.customResolvers = builder.customResolvers;
		this.customHosts = builder.customHosts;
	}

	public boolean isRefreshEveryTime() {
		return refreshEveryTime;
	}

	public DNSResolverMode getDnsResolverMode() {
		return dnsResolverMode;
	}

	public List<String> getCustomResolvers() {
		return customResolvers;
	}

	public List<Map<String, String>> getCustomHosts() {
		return customHosts;
	}

	public static class Builder {
		/**
		 * 是否每迭代都刷新dns中cache缓存
		 */
		private boolean refreshEveryTime;

		/**
		 * dns服务器使用模式
		 */
		private DNSResolverMode dnsResolverMode;

		/**
		 * 如果是自定义模式，这里保存自定义域名解析服务器的地址
		 */
		private List<String> customResolvers;

		/**
		 * 如果是自定义模式，这里保存自定义hosts信息
		 */
		private List<Map<String, String>> customHosts;

		public Builder setRefreshEveryTime(boolean refreshEveryTime) {
			this.refreshEveryTime = refreshEveryTime;
			return this;
		}

		public Builder setDnsResolverMode(DNSResolverMode dnsResolverMode) {
			this.dnsResolverMode = dnsResolverMode;
			return this;
		}

		public Builder setCustomResolvers(List<String> customResolvers) {
			this.customResolvers = customResolvers;
			return this;
		}

		public Builder setCustomHosts(List<Map<String, String>> customHosts) {
			this.customHosts = customHosts;
			return this;
		}

		public Builder addCustomResolver(String resolver) {
			if (this.customResolvers == null) {
				this.customResolvers = new ArrayList<>();
			}
			this.customResolvers.add(resolver);
			return this;
		}

		public Builder addCustomHost(Map<String, String> host) {
			if (this.customHosts == null) {
				this.customHosts = new ArrayList<>();
			}
			this.customHosts.add(host);
			return this;
		}

		public DNSCacheConfig build() {
			return new DNSCacheConfig(this);
		}
	}
}

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

package com.huawei.test.preprocessor.config;

/**
 * 功能描述：HttpURLModifierConfig为url修改配置
 *
 * @author zl
 * @since 2021-12-09
 */
public class HttpURLModifierConfig {
	/**
	 * sessionId对应的名称
	 */
	private final String sessionArgumentName;

	/**
	 * 是否使用分号;来分隔url和sessionId
	 */
	private final boolean useSemicolonSeparator;

	/**
	 * 参数key和value之间是否使用=
	 */
	private final boolean useEqual;

	/**
	 * 是否使用?标识参数部分
	 */
	private final boolean useQuestionMark;

	/**
	 * 是否缓存sessionId
	 */
	private final boolean cacheSessionId;

	/**
	 * 是否对URL进行编码
	 */
	private final boolean encodeUrl;

	private HttpURLModifierConfig(Builder builder) {
		this.sessionArgumentName = builder.sessionArgumentName;
		this.useSemicolonSeparator = builder.useSemicolonSeparator;
		this.useEqual = builder.useEqual;
		this.useQuestionMark = builder.useQuestionMark;
		this.cacheSessionId = builder.cacheSessionId;
		this.encodeUrl = builder.encodeUrl;
	}

	public String getSessionArgumentName() {
		return sessionArgumentName;
	}

	public boolean useSemicolonSeparator() {
		return useSemicolonSeparator;
	}

	public boolean useEqual() {
		return useEqual;
	}

	public boolean useQuestionMark() {
		return useQuestionMark;
	}

	public boolean cacheSessionId() {
		return cacheSessionId;
	}

	public boolean encodeUrl() {
		return encodeUrl;
	}

	public static class Builder {
		/**
		 * sessionId对应的名称
		 */
		private String sessionArgumentName;

		/**
		 * 是否使用分号;来分隔url和sessionId
		 */
		private boolean useSemicolonSeparator;

		/**
		 * 参数key和value之间是否使用=
		 */
		private boolean useEqual;

		/**
		 * 是否使用?标识参数部分
		 */
		private boolean useQuestionMark;

		/**
		 * 是否缓存sessionId
		 */
		private boolean cacheSessionId;

		/**
		 * 是否对URL进行编码
		 */
		private boolean encodeUrl;

		public Builder setSessionArgumentName(String sessionArgumentName) {
			this.sessionArgumentName = sessionArgumentName;
			return this;
		}

		public Builder setUseSemicolonSeparator(boolean useSemicolonSeparator) {
			this.useSemicolonSeparator = useSemicolonSeparator;
			return this;
		}

		public Builder setUseEqual(boolean useEqual) {
			this.useEqual = useEqual;
			return this;
		}

		public Builder setUseQuestionMark(boolean useQuestionMark) {
			this.useQuestionMark = useQuestionMark;
			return this;
		}

		public Builder setCacheSessionId(boolean cacheSessionId) {
			this.cacheSessionId = cacheSessionId;
			return this;
		}

		public Builder setEncodeUrl(boolean encodeUrl) {
			this.encodeUrl = encodeUrl;
			return this;
		}

		public HttpURLModifierConfig build() {
			return new HttpURLModifierConfig(this);
		}
	}
}

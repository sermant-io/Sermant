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

package com.huawei.test.postprocessor.config;

/**
 * 功能描述：正则表达式提取器
 *
 * @author zl
 * @since 2021-12-09
 */
public class RegularExtractorConfig {
	/**
	 * 提取数据正则表达式
	 */
	private final String regularExpression;

	/**
	 * 如果正则表达式中，有多个组，这里需要指定使用那一个组
	 */
	private final int groupIndex;

	/**
	 * 如果正则表达式中能匹配上多个值，这里指定使用第几个值
	 */
	private final int matchIndex;

	/**
	 * 匹配不上的时候使用的默认值
	 */
	private final String defaultValue;

	public String getRegularExpression() {
		return regularExpression;
	}

	public int getGroupIndex() {
		return groupIndex;
	}

	public int getMatchIndex() {
		return matchIndex;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	private RegularExtractorConfig(Builder builder) {
		this.regularExpression = builder.regularExpression;
		this.groupIndex = builder.groupIndex;
		this.matchIndex = builder.matchIndex;
		this.defaultValue = builder.defaultValue;
	}

	@Override
	public String toString() {
		return "RegularExtractorConfig{" +
			"regularExpression='" + regularExpression + '\'' +
			", groupIndex=" + groupIndex +
			", matchIndex=" + matchIndex +
			", defaultValue='" + defaultValue + '\'' +
			'}';
	}

	public static class Builder {
		/**
		 * 提取数据正则表达式
		 */
		private String regularExpression;

		/**
		 * 如果正则表达式中，有多个组，这里需要指定使用那一个组
		 */
		private int groupIndex;

		/**
		 * 如果正则表达式中能匹配上多个值，这里指定使用第几个值
		 */
		private int matchIndex;

		/**
		 * 匹配不上的时候使用的默认值
		 */
		private String defaultValue;

		public Builder setRegularExpression(String regularExpression) {
			this.regularExpression = regularExpression;
			return this;
		}

		public Builder setGroupIndex(int groupIndex) {
			this.groupIndex = groupIndex;
			return this;
		}

		public Builder setMatchIndex(int matchIndex) {
			this.matchIndex = matchIndex;
			return this;
		}

		public Builder setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		public RegularExtractorConfig build() {
			return new RegularExtractorConfig(this);
		}
	}
}

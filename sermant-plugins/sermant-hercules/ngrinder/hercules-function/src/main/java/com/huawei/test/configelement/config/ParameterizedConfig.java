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

package com.huawei.test.configelement.config;

import com.huawei.test.configelement.enums.SharingMode;

import java.util.List;

/**
 * 功能描述：参数化配置
 *
 * @author zl
 * @since 2021-12-08
 */
public class ParameterizedConfig {
	/**
	 * 参数名称,如果未指定参数名称，将会使用文件第一行数据来作为参数名称
	 */
	private final List<String> parameterizedNames;

	/**
	 * 参数化文件
	 */
	private final String parameterizedFile;

	/**
	 * 参数化分隔字符串
	 */
	private final String parameterizedDelimiter;

	/**
	 * 忽略文件第一行数据，只有当parameterizedNames非empty或者null时设置才有用，否则设置不会生效，因为第一行会作为参数名称来处理
	 */
	private final boolean ignoreFirstLine;

	/**
	 * 参数化文件共享模式
	 */
	private final SharingMode sharingMode;

	/**
	 * 是否允许使用双引号解决值带有分隔符的问题，true：允许，这时候每一个值由双引号和分隔符决定，性能会低很多，false：则每一个值只由分隔符决定
	 */
	private final boolean allowQuotedData;

	/**
	 * 获取到文件末尾的时候，是否从头开始取值
	 */
	private final boolean recycleOnEof;

	private ParameterizedConfig(Builder builder) {
		this.parameterizedNames = builder.parameterizedNames;
		this.parameterizedFile = builder.parameterizedFile;
		this.parameterizedDelimiter = builder.parameterizedDelimiter;
		this.ignoreFirstLine = builder.ignoreFirstLine;
		this.sharingMode = builder.sharingMode;
		this.allowQuotedData = builder.allowQuotedData;
		this.recycleOnEof = builder.recycleOnEof;
	}

	public List<String> getParameterizedNames() {
		return parameterizedNames;
	}

	public String getParameterizedFile() {
		return parameterizedFile;
	}

	public String getParameterizedDelimiter() {
		return parameterizedDelimiter;
	}

	public boolean isIgnoreFirstLine() {
		// 只有当parameterizedNames非empty或者null时设置才有用，否则设置不会生效，因为第一行会作为参数名称来处理
		if (this.parameterizedNames == null || this.parameterizedNames.size() == 0) {
			return false;
		}
		return ignoreFirstLine;
	}

	public SharingMode getSharingMode() {
		return sharingMode;
	}

	public boolean isAllowQuotedData() {
		return allowQuotedData;
	}

	public boolean isRecycleOnEof() {
		return recycleOnEof;
	}

	public static class Builder {
		/**
		 * 参数名称,如果未指定参数名称，将会使用文件第一行数据来作为参数名称
		 */
		private List<String> parameterizedNames;

		/**
		 * 参数化文件
		 */
		private String parameterizedFile;

		/**
		 * 参数化分隔字符串
		 */
		private String parameterizedDelimiter;

		/**
		 * 忽略文件第一行数据，只有当parameterizedNames非empty或者null时设置才有用，否则设置不会生效，因为第一行会作为参数名称来处理
		 */
		private boolean ignoreFirstLine;

		/**
		 * 参数化文件共享模式
		 */
		private SharingMode sharingMode;

		/**
		 * 是否允许使用双引号解决值带有分隔符的问题，true：允许，这时候每一个值由双引号和分隔符决定，性能会低很多，false：则每一个值只由分隔符决定
		 */
		private boolean allowQuotedData;

		/**
		 * 获取到文件末尾的时候，是否从头开始取值
		 */
		private boolean recycleOnEof;

		public Builder setParameterizedNames(List<String> parameterizedNames) {
			this.parameterizedNames = parameterizedNames;
			return this;
		}

		public Builder setParameterizedFile(String parameterizedFile) {
			this.parameterizedFile = parameterizedFile;
			return this;
		}

		public Builder setParameterizedDelimiter(String parameterizedDelimiter) {
			this.parameterizedDelimiter = parameterizedDelimiter;
			return this;
		}

		public Builder setIgnoreFirstLine(boolean ignoreFirstLine) {
			this.ignoreFirstLine = ignoreFirstLine;
			return this;
		}

		public Builder setSharingMode(SharingMode sharingMode) {
			this.sharingMode = sharingMode;
			return this;
		}

		public Builder setAllowQuotedData(boolean allowQuotedData) {
			this.allowQuotedData = allowQuotedData;
			return this;
		}

		public Builder setRecycleOnEof(boolean recycleOnEof) {
			this.recycleOnEof = recycleOnEof;
			return this;
		}

		public ParameterizedConfig build() {
			return new ParameterizedConfig(this);
		}
	}
}

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

package com.huawei.test.configelement.impl;

import com.huawei.test.configelement.Parameterized;
import com.huawei.test.configelement.config.ParameterizedConfig;

/**
 * 功能描述：csv格式数据参数化实现
 *
 * @author zl
 * @since 2021-12-08
 */
public class CsvParameterized extends Parameterized {
	/**
	 * 参数化配置
	 */
	private ParameterizedConfig config;

	@Override
	public void initConfig(ParameterizedConfig config) {
		this.config = config;
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public String[] nextLineValue() {
		return new String[0];
	}

	@Override
	public String nextSpecifyValue(String paramKey) {
		return null;
	}

	@Override
	public String nextSpecifyValue(int valueIndex) {
		return null;
	}

	@Override
	public String[][] obtainAllValues() {
		return new String[0][];
	}
}

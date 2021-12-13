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

package com.huawei.test.configelement;

import com.huawei.test.PressureTestFunction;

/**
 * 功能描述：需要通过配置初始化的函数
 *
 * @author zl
 * @since 2021-12-09
 */
public abstract class ConfigElement<T> implements PressureTestFunction {
	/**
	 * 函数名称
	 */
	private String functionName;

	/**
	 * 函数注释
	 */
	private String functionComments;

	/**
	 * 初始化函数配置
	 *
	 * @param config 配置信息
	 */
	public abstract void initConfig(T config);

	@Override
	public void defineFunctionName(String name) {

	}

	@Override
	public void addComments(String comments) {

	}
}

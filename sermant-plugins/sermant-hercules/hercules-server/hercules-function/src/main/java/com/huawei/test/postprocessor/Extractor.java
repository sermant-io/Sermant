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

package com.huawei.test.postprocessor;

import com.huawei.test.PressureTestFunction;

/**
 * 功能描述：提取器基类
 *
 * @author zl
 * @since 2021-12-09
 */
public abstract class Extractor<T> implements PressureTestFunction {
	/**
	 * 初始化提取器配置
	 *
	 * @param content 需要提取数据的内容
	 * @param extractorConfig 提取器配置
	 * @return 提取的数据
	 */
	public abstract String extract(String content, T extractorConfig);

	@Override
	public void defineFunctionName(String name) {

	}

	@Override
	public void addComments(String comments) {

	}
}

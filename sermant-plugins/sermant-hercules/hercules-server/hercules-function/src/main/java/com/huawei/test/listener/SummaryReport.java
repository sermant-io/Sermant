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

package com.huawei.test.listener;

import com.huawei.test.PressureTestFunction;
import com.huawei.test.listener.config.SummaryReportConfig;

/**
 * 功能描述：
 *
 * @author zl
 * @since 2021-12-09
 */
public abstract class SummaryReport implements PressureTestFunction {
	/**
	 * 初始化总结报告配置
	 *
	 * @param summaryReportConfig 总结报告配置
	 */
	public abstract void initConfig(SummaryReportConfig summaryReportConfig);

	@Override
	public void defineFunctionName(String name) {

	}

	@Override
	public void addComments(String comments) {

	}
}

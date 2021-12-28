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

package com.huawei.test;

/**
 * 功能描述：压测函数基类
 *
 * @author zl
 * @since 2021-12-14
 */
public class BasePressureTestFunction implements PressureTestFunction{
	/**
	 * 函数名称
	 */
	private String functionName;

	/**
	 * 函数说明
	 */
	private String comments;

	@Override
	public void defineFunctionName(String name) {
		this.functionName = name;
	}

	@Override
	public void addComments(String comments) {
		this.comments = comments;
	}

	public String getFunctionName() {
		return functionName;
	}

	public String getComments() {
		return comments;
	}
}

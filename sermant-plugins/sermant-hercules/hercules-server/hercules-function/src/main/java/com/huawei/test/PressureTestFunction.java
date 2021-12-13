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

package com.huawei.test;

/**
 * 功能描述：压测函数接口
 *
 * @author zl
 * @since 2021-12-08
 */
public interface PressureTestFunction {
	/**
	 * 定义函数名称
	 *
	 * @param name 函数名称
	 */
	void defineFunctionName(String name);

	/**
	 * 定义函数说明
	 *
	 * @param comments 函数说明
	 */
	void addComments(String comments);
}

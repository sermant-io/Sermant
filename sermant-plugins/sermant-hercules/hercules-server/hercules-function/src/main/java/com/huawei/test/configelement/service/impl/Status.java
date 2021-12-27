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

package com.huawei.test.configelement.service.impl;

/**
 * 功能描述：参数化文件中，如果能使用双引号包括数据时，状态机解析数据时定义的字符状态类型
 *
 * @author zl
 * @since 2021-12-17
 */
public enum Status {
	/**
	 * 整行数据开始
	 */
	START,

	/**
	 * 双引号开始的引号
	 */
	QUOT_DATA_START,

	/**
	 * 双引号之间的数据字符
	 */
	QUOT_DATA,

	/**
	 * 双引号结束的引号
	 */
	QUOT_DATA_END,

	/**
	 * 非双引号数据字符
	 */
	NO_QUOT_DATA,

	/**
	 * 字段分隔符
	 */
	DATA_DELIMIT,

	/**
	 * 双引号中反斜线处理
	 */
	QUOT_ORDINARY_CHECK,

	/**
	 * 非双引号数据中反斜线处理
	 */
	NO_QUOT_ORDINARY_CHECK,

	/**
	 * 整行数据处理结束
	 */
	END
}

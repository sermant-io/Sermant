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

import com.huawei.test.configelement.BaseParameterized;
import com.huawei.test.configelement.service.IParamFileLineSplitter;
import com.huawei.test.configelement.service.impl.CommonParamFileLineSplitter;
import com.huawei.test.configelement.service.impl.QuotedDataParamFileLineSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 功能描述：csv格式数据参数化实现
 *
 * @author zl
 * @since 2021-12-08
 */
public class CsvParameterized extends BaseParameterized {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CsvParameterized.class);

	@Override
	protected List<String> splitLineContent(String lineContent, String delimiter, boolean quotData) {
		IParamFileLineSplitter paramFileLineSplitter;
		if (quotData) {
			paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		} else {
			paramFileLineSplitter = new CommonParamFileLineSplitter();
		}
		return paramFileLineSplitter.splitLine(lineContent, delimiter);
	}
}

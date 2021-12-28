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

import com.huawei.test.configelement.service.IParamFileLineSplitter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 功能描述：一行文件内容按照分隔符，直接分割，然后返回分割之后的结果，不用考虑值里面有分隔符之类的情况
 *
 * @author zl
 * @since 2021-12-17
 */
public class CommonParamFileLineSplitter implements IParamFileLineSplitter {
	@Override
	public List<String> splitLine(String lineContent, String delimiter) {
		if (StringUtils.isEmpty(lineContent)) {
			return Collections.emptyList();
		}
		List<String> valuesList = new ArrayList<>();
		if (StringUtils.isEmpty(delimiter)) {
			valuesList.add(lineContent);
			return valuesList;
		}
		String[] valuesArray = lineContent.split(delimiter, -1);
		valuesList.addAll(Arrays.asList(valuesArray));
		return valuesList;
	}
}

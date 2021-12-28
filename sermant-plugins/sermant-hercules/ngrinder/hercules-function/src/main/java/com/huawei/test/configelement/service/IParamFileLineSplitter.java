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

package com.huawei.test.configelement.service;

import java.util.List;

/**
 * 功能描述：一行文件内容分割接口
 *
 * @author zl
 * @since 2021-12-17
 */
public interface IParamFileLineSplitter {
	/**
	 * 对一行内容按照指定的分隔符进行分割
	 *
	 * @param lineContent 一行文件内容
	 * @param delimiter   分隔符
	 * @return 分割之后的文件字符串
	 */
	List<String> splitLine(String lineContent, String delimiter);
}

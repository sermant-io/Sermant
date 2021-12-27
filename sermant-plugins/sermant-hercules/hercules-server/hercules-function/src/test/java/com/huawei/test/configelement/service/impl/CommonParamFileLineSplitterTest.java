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
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CommonParamFileLineSplitterTest {
	@Test
	public void test_splitLine_when_lineContent_is_null() {
		IParamFileLineSplitter paramFileLineSplitter = new CommonParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.splitLine(null, ",");
		Assert.assertEquals(0, strings.size());
	}

	@Test
	public void test_splitLine_when_lineContent_is_empty() {
		IParamFileLineSplitter paramFileLineSplitter = new CommonParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.splitLine("", ",");
		Assert.assertEquals(0, strings.size());
	}

	@Test
	public void test_splitLine_when_delimiter_is_empty() {
		IParamFileLineSplitter paramFileLineSplitter = new CommonParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.splitLine("aaa,aaa", "");
		Assert.assertEquals("aaa,aaa", strings.get(0));
	}

	@Test
	public void test_splitLine_when_delimiter_is_null() {
		IParamFileLineSplitter paramFileLineSplitter = new CommonParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.splitLine("aaa,aaa", null);
		Assert.assertEquals("aaa,aaa", strings.get(0));
	}

	@Test
	public void test_splitLine_when_lineContent_is_valid() {
		IParamFileLineSplitter paramFileLineSplitter = new CommonParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.splitLine("aaa,bbb", ",");
		Assert.assertEquals("aaa", strings.get(0));
		Assert.assertEquals("bbb", strings.get(1));
	}

	@Test
	public void test_splitLine_when_lineContent_has_empty_partition() {
		IParamFileLineSplitter paramFileLineSplitter = new CommonParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.splitLine("aaa,bbb,,", ",");
		Assert.assertEquals("aaa", strings.get(0));
		Assert.assertEquals("bbb", strings.get(1));
		Assert.assertEquals("", strings.get(2));
		Assert.assertEquals("", strings.get(3));
		Assert.assertEquals(4, strings.size());
	}
}

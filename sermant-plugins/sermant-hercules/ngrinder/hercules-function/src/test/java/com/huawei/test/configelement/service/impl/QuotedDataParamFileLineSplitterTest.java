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

import com.huawei.test.exception.FunctionException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class QuotedDataParamFileLineSplitterTest {

	@Test
	public void test_splitLine_when_lineContent_is_empty() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.splitLine(null, ",");
		Assert.assertEquals(0 , strings.size());
	}

	@Test
	public void test_splitLine_when_delimiter_is_empty() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.splitLine("line", "");
		Assert.assertEquals("line", strings.get(0));
	}

	@Test
	public void test_doSplit_when_lineContent_is_null() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.doSplit(null, ',');
		Assert.assertTrue(strings.isEmpty());
	}

	@Test
	public void test_doSplit_when_lineContent_is_empty() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.doSplit(null, ',');
		Assert.assertTrue(strings.isEmpty());
	}

	@Test
	public void test_doSplit_when_lineContent_has_no_quot() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.doSplit("aaa,bbb,ccc", ',');
		Assert.assertEquals(3, strings.size());
		Assert.assertEquals("aaa", strings.get(0));
		Assert.assertEquals("bbb", strings.get(1));
		Assert.assertEquals("ccc", strings.get(2));
	}

	@Test
	public void test_doSplit_when_lineContent_has_no_quot_and_last_delimiter() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.doSplit("aaa,bbb,ccc,,", ',');
		Assert.assertEquals(5, strings.size());
		Assert.assertEquals("aaa", strings.get(0));
		Assert.assertEquals("bbb", strings.get(1));
		Assert.assertEquals("ccc", strings.get(2));
		Assert.assertEquals("", strings.get(3));
		Assert.assertEquals("", strings.get(4));
	}

	@Test
	public void test_doSplit_when_lineContent_has_no_quot_and_empty_data() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.doSplit(",aaa,,ccc,,", ',');
		Assert.assertEquals(6, strings.size());
		Assert.assertEquals("", strings.get(0));
		Assert.assertEquals("aaa", strings.get(1));
		Assert.assertEquals("", strings.get(2));
		Assert.assertEquals("ccc", strings.get(3));
		Assert.assertEquals("", strings.get(4));
		Assert.assertEquals("", strings.get(5));
	}

	@Test
	public void test_doSplit_when_lineContent_has_no_quot_and_empty_data_and_backslash() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.doSplit("\\,,aaa,,c\\,cc,,\\\"", ',');
		Assert.assertEquals(6, strings.size());
		Assert.assertEquals(",", strings.get(0));
		Assert.assertEquals("aaa", strings.get(1));
		Assert.assertEquals("", strings.get(2));
		Assert.assertEquals("c,cc", strings.get(3));
		Assert.assertEquals("", strings.get(4));
		Assert.assertEquals("\"", strings.get(5));
	}

	@Test
	public void test_doSplit_when_lineContent_has_quot_at_start() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.doSplit("\"s\",aa,bb", ',');
		Assert.assertEquals(3, strings.size());
		Assert.assertEquals("s", strings.get(0));
		Assert.assertEquals("aa", strings.get(1));
		Assert.assertEquals("bb", strings.get(2));
	}

	@Test
	public void test_doSplit_when_lineContent_has_quot_at_end() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.doSplit("s,aa,\"bb\"", ',');
		Assert.assertEquals(3, strings.size());
		Assert.assertEquals("s", strings.get(0));
		Assert.assertEquals("aa", strings.get(1));
		Assert.assertEquals("bb", strings.get(2));
	}

	@Test
	public void test_doSplit_when_lineContent_has_quot_at_middle() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.doSplit("s,\"aa\",bb", ',');
		Assert.assertEquals(3, strings.size());
		Assert.assertEquals("s", strings.get(0));
		Assert.assertEquals("aa", strings.get(1));
		Assert.assertEquals("bb", strings.get(2));
	}

	@Test
	public void test_doSplit_when_lineContent_has_quot_and_quot_data_include_delimiter() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.doSplit("\"s,\",\"a,a\",\",bb\"", ',');
		Assert.assertEquals(3, strings.size());
		Assert.assertEquals("s,", strings.get(0));
		Assert.assertEquals("a,a", strings.get(1));
		Assert.assertEquals(",bb", strings.get(2));
	}

	@Test
	public void test_doSplit_when_lineContent_has_quot_and_quot_data_include_delimiter_and_include_quot() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.doSplit("\"s,\\\"\",\"a,\\\"a\",\"\\\",bb\"", ',');
		Assert.assertEquals(3, strings.size());
		Assert.assertEquals("s,\"", strings.get(0));
		Assert.assertEquals("a,\"a", strings.get(1));
		Assert.assertEquals("\",bb", strings.get(2));
	}

	@Test(expected = FunctionException.class)
	public void test_doSplit_when_has_quot_in_no_quot_data() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.doSplit("aa,b\"b", ',');
		Assert.assertEquals(2, strings.size());
		Assert.assertEquals("aa", strings.get(0));
		Assert.assertEquals("b\"b", strings.get(1));
	}

	@Test(expected = FunctionException.class)
	public void test_doSplit_when_has_quot_in_data() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.doSplit("aa,\"b\"b\"", ',');
		Assert.assertEquals(2, strings.size());
		Assert.assertEquals("aa", strings.get(0));
		Assert.assertEquals("b\"b", strings.get(1));
	}

	@Test(expected = FunctionException.class)
	public void test_doSplit_when_has_not_start_quot() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.doSplit("aa,bb\"", ',');
		Assert.assertEquals(2, strings.size());
		Assert.assertEquals("aa", strings.get(0));
		Assert.assertEquals("b\"b", strings.get(1));
	}

	@Test(expected = FunctionException.class)
	public void test_doSplit_when_has_not_end_quot() {
		QuotedDataParamFileLineSplitter paramFileLineSplitter = new QuotedDataParamFileLineSplitter();
		List<String> strings = paramFileLineSplitter.doSplit("aa,\"bb", ',');
		Assert.assertEquals(2, strings.size());
		Assert.assertEquals("aa", strings.get(0));
		Assert.assertEquals("b\"b", strings.get(1));
	}
}

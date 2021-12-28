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

package com.huawei.test.postprocessor.impl;

import com.huawei.test.postprocessor.config.RegularExtractorConfig;
import org.junit.Assert;
import org.junit.Test;

public class RegularExpressionExtractorTest {
	@Test
	public void test_extract_when_content_is_null() {
		RegularExpressionExtractor regularExpressionExtractor = new RegularExpressionExtractor();
		String result = regularExpressionExtractor.extract(null, new RegularExtractorConfig.Builder().build());
		Assert.assertEquals("", result);
	}

	@Test
	public void test_extract_when_content_is_empty() {
		RegularExpressionExtractor regularExpressionExtractor = new RegularExpressionExtractor();
		String result = regularExpressionExtractor.extract("", new RegularExtractorConfig.Builder().build());
		Assert.assertEquals("", result);
	}

	@Test
	public void test_extract_when_extractorConfig_is_null() {
		RegularExpressionExtractor regularExpressionExtractor = new RegularExpressionExtractor();
		String result = regularExpressionExtractor.extract("AcceptType:application/json", null);
		Assert.assertEquals("", result);
	}

	@Test
	public void test_extract_when_regularExpression_in_extractorConfig_is_null() {
		RegularExpressionExtractor regularExpressionExtractor = new RegularExpressionExtractor();
		RegularExtractorConfig extractorConfig = new RegularExtractorConfig.Builder()
			.setDefaultValue("")
			.setRegularExpression(null)
			.setGroupIndex(1)
			.setMatchIndex(1)
			.build();
		String result = regularExpressionExtractor.extract("AcceptType:application/json", extractorConfig);
		Assert.assertEquals("", result);
	}

	@Test
	public void test_extract_when_regularExpression_in_extractorConfig_is_empty() {
		RegularExpressionExtractor regularExpressionExtractor = new RegularExpressionExtractor();
		RegularExtractorConfig extractorConfig = new RegularExtractorConfig.Builder()
			.setDefaultValue("")
			.setRegularExpression("")
			.setGroupIndex(1)
			.setMatchIndex(1)
			.build();
		String result = regularExpressionExtractor.extract("AcceptType:application/json", extractorConfig);
		Assert.assertEquals("", result);
	}

	@Test
	public void test_extract_when_matchIndex_in_extractorConfig_is_invalid() {
		RegularExpressionExtractor regularExpressionExtractor = new RegularExpressionExtractor();
		String defaultValue = "defaultValue";
		RegularExtractorConfig extractorConfig = new RegularExtractorConfig.Builder()
			.setDefaultValue(defaultValue)
			.setRegularExpression("(\\d+)")
			.setGroupIndex(1)
			.setMatchIndex(1)
			.build();
		String result = regularExpressionExtractor.extract("AcceptType:application/json", extractorConfig);
		Assert.assertEquals(defaultValue, result);
	}

	@Test
	public void test_extract_when_groupIndex_in_extractorConfig_is_invalid() {
		RegularExpressionExtractor regularExpressionExtractor = new RegularExpressionExtractor();
		String defaultValue = "defaultValue";
		RegularExtractorConfig extractorConfig = new RegularExtractorConfig.Builder()
			.setDefaultValue(defaultValue)
			.setRegularExpression("([a-z]+)")
			.setGroupIndex(2)
			.setMatchIndex(1)
			.build();
		String result = regularExpressionExtractor.extract("AcceptType:application/json", extractorConfig);
		Assert.assertEquals(defaultValue, result);
	}

	@Test
	public void test_extract_when_all_param_is_valid() {
		RegularExpressionExtractor regularExpressionExtractor = new RegularExpressionExtractor();
		String defaultValue = "defaultValue";
		RegularExtractorConfig extractorConfig = new RegularExtractorConfig.Builder()
			.setDefaultValue(defaultValue)
			.setRegularExpression("/([a-z]+)")
			.setGroupIndex(1)
			.setMatchIndex(1)
			.build();
		String result = regularExpressionExtractor.extract("AcceptType:application/json", extractorConfig);
		Assert.assertEquals("json", result);
	}
}

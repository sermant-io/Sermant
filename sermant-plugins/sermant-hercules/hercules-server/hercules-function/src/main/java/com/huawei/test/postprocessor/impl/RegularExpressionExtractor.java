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

import com.huawei.test.postprocessor.Extractor;
import com.huawei.test.postprocessor.config.RegularExtractorConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 功能描述：正则表达式提取数据逻辑实现
 *
 * @author zl
 * @since 2021-12-09
 */
public class RegularExpressionExtractor extends Extractor<RegularExtractorConfig> {
	/**
	 * 日志
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(RegularExpressionExtractor.class);

	@Override
	public String extract(String content, RegularExtractorConfig extractorConfig) {
		LOGGER.debug("Extract data by regular expression, content:{}, config:{}", content, extractorConfig);
		if (StringUtils.isEmpty(content)) {
			LOGGER.warn("The content used for extracting is an empty string.");
			return "";
		}
		if (extractorConfig == null) {
			LOGGER.warn("The config used for extracting is null.");
			return "";
		}
		String regularExpression = extractorConfig.getRegularExpression();
		if (StringUtils.isEmpty(regularExpression)) {
			LOGGER.warn("The regular expression used for extracting is empty.");
			return "";
		}
		String defaultValue = extractorConfig.getDefaultValue();
		int matchIndex = extractorConfig.getMatchIndex();
		Pattern pattern = Pattern.compile(regularExpression);
		Matcher matcher = pattern.matcher(content);
		for (int i = 0; i < matchIndex; i++) {
			// 一直匹配配置中指定的次数，如果不能支持到匹配的次数，则直接返回默认值
			if (!matcher.find()) {
				LOGGER.error("Regular expressions cannot match the number of times[{}].", matchIndex);
				return defaultValue;
			}
		}
		int groupIndex = extractorConfig.getGroupIndex();
		try {
			String group = matcher.group(groupIndex);
			return StringUtils.isEmpty(group) ? defaultValue : group;
		} catch (Exception e) {
			LOGGER.error("Occur an error when extract data, message:{}.", e.getMessage());
			return defaultValue;
		}
	}
}

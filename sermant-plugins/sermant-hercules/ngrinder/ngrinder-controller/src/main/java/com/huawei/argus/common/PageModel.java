/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.argus.common;

import java.util.List;

/**
 * 功能描述：分页查询数据模型
 *
 *
 * @since 2021-10-15
 */
public class PageModel<T> {
	/**
	 * 元素查询结果列表
	 */
	private final List<T> pageContent;

	/**
	 * 元素总共存在页数
	 */
	private final int totalPages;

	/**
	 * 元素总个数
	 */
	private final long totalCount;

	public PageModel(List<T> pageContent, int totalPages, long totalCount) {
		this.pageContent = pageContent;
		this.totalPages = totalPages;
		this.totalCount = totalCount;
	}

	public List<T> getPageContent() {
		return pageContent;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public long getTotalCount() {
		return totalCount;
	}
}

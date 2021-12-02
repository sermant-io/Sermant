/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.argus.common;

import java.util.List;

/**
 * 功能描述：分页查询数据模型
 *
 * @author z30009938
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

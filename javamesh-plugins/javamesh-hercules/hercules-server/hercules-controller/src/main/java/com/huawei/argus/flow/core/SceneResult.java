package com.huawei.argus.flow.core;

import java.util.List;

/**
 * 场景执行结果
 * Created by x00377290 on 2019/4/23.
 */
public class SceneResult {

	public List<ApiResult> getApiResultList() {
		return apiResultList;
	}

	public void setApiResultList(List<ApiResult> apiResultList) {
		this.apiResultList = apiResultList;
	}

	private List<ApiResult> apiResultList;
}

package com.huawei.argus.flow.core;

import java.util.List;

/**
 * API 执行结果
 * Created by x00377290 on 2019/4/23.
 */
public class ApiResult {

	public List<CheckPointResult> getCheckPointResultList() {
		return checkPointResultList;
	}

	public void setCheckPointResultList(List<CheckPointResult> checkPointResultList) {
		this.checkPointResultList = checkPointResultList;
	}

	List<CheckPointResult> checkPointResultList;

}

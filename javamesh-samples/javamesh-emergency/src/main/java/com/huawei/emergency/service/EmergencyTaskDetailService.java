/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service;

import com.huawei.common.api.CommonResult;

/**
 * @author y30010171
 * @since 2021-11-04
 **/
public interface EmergencyTaskDetailService extends EmergencyCallBack {
    CommonResult ensure(int recordId,String result,String userName);
}

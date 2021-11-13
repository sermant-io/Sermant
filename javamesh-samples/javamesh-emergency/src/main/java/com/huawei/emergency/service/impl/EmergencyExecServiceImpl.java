/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service.impl;

import com.huawei.emergency.mapper.EmergencyExecMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 执行记录管理
 *
 * @author y30010171
 * @since 2021-11-09
 **/
@Service
public class EmergencyExecServiceImpl {

    @Autowired
    EmergencyExecMapper execMapper;


}

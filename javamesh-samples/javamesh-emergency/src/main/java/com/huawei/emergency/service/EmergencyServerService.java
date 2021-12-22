/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service;

import com.huawei.common.api.CommonPage;
import com.huawei.common.api.CommonResult;
import com.huawei.emergency.entity.EmergencyPlan;
import com.huawei.emergency.entity.EmergencyServer;

/**
 * 服务器信息管理
 *
 * @author y30010171
 * @since 2021-11-29
 **/
public interface EmergencyServerService extends EmergencyCommonService<EmergencyServer> {

    /**
     * 从平台获取该服务器所有可用账号
     *
     * @param serverIp
     * @return
     */
    CommonResult allServerUser(String serverIp);

    /**
     * 查询已创建的服务器信息
     *
     * @param params
     * @return
     */
    CommonResult queryServerInfo(CommonPage<EmergencyServer> params, String keyword, int[] excludeServerIds);

    CommonResult search(String serverName);

    CommonResult license(EmergencyServer server);

    CommonResult deleteServerList(String[] serverIds, String userName);
}

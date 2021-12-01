/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.fallback;

import com.huawei.hercules.service.agent.IAgentManagerService;
import org.springframework.stereotype.Component;

/**
 * 功能描述：agent管理后端feign调用失败回调
 *
 * @author z30009938
 * @since 2021-11-24
 */
@Component
public class AgentManagerServiceFallbackFactory extends BaseFeignFallbackFactory<IAgentManagerService> {
    @Override
    public IAgentManagerService create(Throwable throwable) {
        return error(throwable);
    }
}

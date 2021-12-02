/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.fallback;

import com.huawei.hercules.service.agent.IAgentDownloadService;
import org.springframework.stereotype.Component;

/**
 * 功能描述：agentDownload调用feign失败回调
 *
 * @author z30009938
 * @since 2021-11-24
 */
@Component
public class AgentDownLoadServiceFallbackFactory extends BaseFeignFallbackFactory<IAgentDownloadService> {
    @Override
    public IAgentDownloadService create(Throwable throwable) {
        return error(throwable);
    }
}

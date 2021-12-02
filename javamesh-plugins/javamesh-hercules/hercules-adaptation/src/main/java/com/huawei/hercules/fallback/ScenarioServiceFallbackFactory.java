/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.fallback;

import com.huawei.hercules.service.scenario.IScenarioService;
import org.springframework.stereotype.Component;

/**
 * 功能描述：场景后端feign调用失败时回调
 *
 * @author z30009938
 * @since 2021-11-24
 */
@Component
public class ScenarioServiceFallbackFactory extends BaseFeignFallbackFactory<IScenarioService> {
    @Override
    public IScenarioService create(Throwable throwable) {
        return error(throwable);
    }
}

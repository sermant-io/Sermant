/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.fallback;

import com.huawei.hercules.service.script.IScriptService;
import org.springframework.stereotype.Component;

/**
 * 功能描述：脚本feign调用失败回调
 *
 * @author z30009938
 * @since 2021-11-24
 */
@Component
public class ScriptServiceFallbackFactory extends BaseFeignFallbackFactory<IScriptService> {
    @Override
    public IScriptService create(Throwable throwable) {
        return error(throwable);
    }
}

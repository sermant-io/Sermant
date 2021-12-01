/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.fallback;

import com.huawei.hercules.service.ILoginService;
import org.springframework.stereotype.Component;

/**
 * 功能描述：ILoginService 对象的fallback
 *
 * @author z30009938
 * @since 2021-11-23
 */
@Component
public class LoginServiceFallbackFactory extends BaseFeignFallbackFactory<ILoginService> {
    @Override
    public ILoginService create(Throwable throwable) {
        return error(throwable);
    }
}

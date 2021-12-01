/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.fallback;

import com.huawei.hercules.service.influxdb.IHostApplicationMapping;
import org.springframework.stereotype.Component;

/**
 * 功能描述：Host和服务映射请求失败回调工厂
 *
 * @author z30009938
 * @since 2021-11-22
 */
@Component
public class HostAppMappingFallbackFactory extends BaseFeignFallbackFactory<IHostApplicationMapping> {
    @Override
    public IHostApplicationMapping create(Throwable throwable) {
        return error(throwable);
    }
}

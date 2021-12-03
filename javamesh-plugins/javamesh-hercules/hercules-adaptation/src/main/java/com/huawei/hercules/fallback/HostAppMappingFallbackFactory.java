/*
 * Copyright (C) Huawei Technologies Co., Ltd. $YEAR$-$YEAR$. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

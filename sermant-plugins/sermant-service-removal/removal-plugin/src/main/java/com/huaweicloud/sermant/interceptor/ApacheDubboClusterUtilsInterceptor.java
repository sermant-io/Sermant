/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.interceptor;

import com.huaweicloud.sermant.common.RemovalConstants;
import com.huaweicloud.sermant.core.utils.StringUtils;

import org.apache.dubbo.common.URL;

/**
 * 增强ClusterUtils类的mergeUrl方法
 *
 * @author zhp
 * @since 2023-02-17
 */
public class ApacheDubboClusterUtilsInterceptor extends AbstractClusterUtilsInterceptor<URL> {
    @Override
    protected String getInterfaceName(URL url) {
        return StringUtils.getString(url.getServiceInterface());
    }

    @Override
    protected String getServiceName(URL url) {
        return StringUtils.getString(url.getParameter(RemovalConstants.APPLICATION_KEY));
    }
}

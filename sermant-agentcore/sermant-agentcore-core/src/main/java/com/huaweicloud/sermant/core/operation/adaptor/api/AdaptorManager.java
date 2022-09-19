/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.operation.adaptor.api;

import com.huaweicloud.sermant.core.operation.BaseOperation;

import java.lang.instrument.Instrumentation;
import java.util.List;

/**
 * 适配器管理器接口
 *
 * @author luanwenfei
 * @since 2022-06-27
 */
public interface AdaptorManager extends BaseOperation {
    /**
     * 初始化所有适配器
     *
     * @param adaptorNames    适配器名称集
     * @param instrumentation Instrumentation对象
     * @return 是否初始化成功
     */
    boolean initAdaptors(List<String> adaptorNames, Instrumentation instrumentation);
}

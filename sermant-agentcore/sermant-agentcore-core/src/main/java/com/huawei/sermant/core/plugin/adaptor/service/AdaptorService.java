/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.plugin.adaptor.service;

import java.io.File;
import java.lang.instrument.Instrumentation;

/**
 * 适配器服务接口
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public interface AdaptorService {
    /**
     * 初始化适配器服务
     *
     * @param agentMainArg    外部agent启动参数
     * @param execEnvDir      适配器运行环境目录
     * @param classLoader     加载适配包的类加载器
     * @param instrumentation Instrumentation对象
     * @return 是否启动服务成功
     */
    boolean start(String agentMainArg, File execEnvDir, ClassLoader classLoader, Instrumentation instrumentation);

    /**
     * 终止服务
     */
    void stop();
}

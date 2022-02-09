/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.agent;

import com.huawei.sermant.core.agent.annotations.AboutDelete;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * byte buddy增强监听器，用于保存增强后的字节码
 * <p> Copyright 2021
 *
 * @since 2021
 */
@AboutDelete
@Deprecated
class LoadListener implements AgentBuilder.Listener {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final String exportPath = System.getProperty("apm.agent.class.export.path");

    @Override
    public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
    }

    @SuppressWarnings("checkstyle:RegexpSinglelineJava")
    @Override
    public void onTransformation(
            final TypeDescription typeDescription,
            final ClassLoader classLoader,
            final JavaModule module,
            final boolean loaded,
            final DynamicType dynamicType) {
        try {
            if (StringUtils.isNotBlank(exportPath)) {
                dynamicType.saveIn(new File(exportPath));
            }
        } catch (IOException e) {
            LOGGER.warning(String.format("save class {%s} byte code failed", typeDescription.getTypeName()));
        }
    }

    @Override
    public void onIgnored(
            final TypeDescription typeDescription,
            final ClassLoader classLoader,
            final JavaModule module,
            final boolean loaded) {
    }

    @Override
    public void onError(
            final String typeName,
            final ClassLoader classLoader,
            final JavaModule module,
            final boolean loaded,
            final Throwable throwable) {
    }

    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
    }
}

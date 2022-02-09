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

package com.huawei.sermant.core.agent.transformer;

import com.huawei.sermant.core.agent.annotations.AboutDelete;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

/**
 * 通用转换器
 * <p>当被增强类为启动类加载器加载的类时，移交{@link BootstrapTransformer}处理，否则移交{@link DelegateTransformer}
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/12
 */
@AboutDelete
@Deprecated
public class CommonTransformer implements AgentBuilder.Transformer {
    private final BootstrapTransformer bootstrapTransformer;
    private final DelegateTransformer delegateTransformer;

    public CommonTransformer() {
        this.bootstrapTransformer = new BootstrapTransformer();
        this.delegateTransformer = new DelegateTransformer();
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule module) {
        if (classLoader == null) {
            return bootstrapTransformer.transform(builder, typeDescription, null, module);
        } else {
            return delegateTransformer.transform(builder, typeDescription, classLoader, module);
        }
    }
}

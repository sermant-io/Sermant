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

package com.huawei.sermant.core.lubanops.bootstrap;

import com.huawei.sermant.core.agent.definition.TopListener;

import java.util.List;
import java.util.Set;

public interface Listener extends TopListener {

    /**
     * 初始化方法
     */
    void init();

    /**
     * 需要拦截的类
     * @return
     */
    Set<String> getClasses();

    /**
     * 需要拦截的方法
     * @return
     */
    List<TransformerMethod> getTransformerMethod();

    /**
     * 是否添加自定义属性字段 通过com.lubanops.apm.bootstrap.TransformAccess来操作自定义字段
     * @return
     */
    boolean hasAttribute();

    /**
     * 是否添加获取拦截类字段方法 通过com.lubanops.apm.bootstrap.AttributeAccess来获取字段值
     * 按添加顺序放在getLopsFileds获取的数组中
     * @return
     */
    List<String> getFields();

    /**
     * 添加采集器tag
     */
    void addTag();

}

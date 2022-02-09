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

package com.huawei.example.demo.common;

import com.huawei.sermant.core.plugin.agent.annotations.BeanPropertyFlag;

/**
 * 测试为被增强类添加JavaBean接口，增添JavaBean属性并为之设置get，set方法
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-22
 */
@BeanPropertyFlag(value = "foo", type = String.class)
@BeanPropertyFlag(value = "bar", type = int.class)
public interface DemoBeanPropertyApi {
    /**
     * foo字段的get方法
     *
     * @return foo字段值
     */
    String getFoo();

    /**
     * foo字段的set方法
     *
     * @param foo foo字段值
     */
    void setFoo(String foo);

    /**
     * bar字段的get方法
     *
     * @return bar字段值
     */
    int getBar();

    /**
     * bar字段的set方法
     *
     * @param bar bar字段值
     */
    void setBar(int bar);
}

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

package com.huawei.example.demo.config;

import com.huawei.sermant.core.config.common.ConfigFieldKey;

/**
 * 插件配置中的复杂对象示例
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-16
 */
public class DemoPojo {
    /**
     * 普通属性
     */
    private int intField;

    /**
     * 起别名的属性，将作为检验别名是否生效的依据
     */
    @ConfigFieldKey("strField") // 修改参数名称后缀
    private String str;

    public int getIntField() {
        return intField;
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return "DemoPojo{"
                + "intField=" + intField
                + ", str='" + str + '\''
                + '}';
    }
}

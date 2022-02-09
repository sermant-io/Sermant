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

package com.huawei.sermant.core.config.common;

/**
 * 配置基类
 * <p>要求所有配置类都继承该配置基类，同时在{@code META-INF/services}目录创建{@link BaseConfig}文件，并添加所有需要加载的实现类
 * <p>之后，调用{@link com.huawei.sermant.core.config.ConfigManager#initialize(java.util.Map)}初始化所有配置对象
 * <p>初始化之后，调用{@link com.huawei.sermant.core.config.ConfigManager#getConfig(Class)}获取配置对象
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-08-19
 */
public interface BaseConfig {

}

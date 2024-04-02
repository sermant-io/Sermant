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

package com.huaweicloud.sermant.core.config.common;

import com.huaweicloud.sermant.core.config.ConfigManager;

/**
 * BaseConfig
 * <p>All configuration classes are required to inherit from this configuration base class, and the
 * {@link BaseConfig} file is created in the {@code META-INF/services} directory and all implementation classes that
 * need to be loaded are added
 * <p>After that, call {@link ConfigManager#initialize(java.util.Map)} to initialize all configuration objects
 * <p>After initialization, call {@link ConfigManager#getConfig(Class)} to get the configuration object
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-08-19
 */
public interface BaseConfig {

}

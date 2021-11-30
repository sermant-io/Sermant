/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.config.common;

/**
 * 配置基类
 * <p>要求所有配置类都继承该配置基类，同时在{@code META-INF/services}目录创建{@link BaseConfig}文件，并添加所有需要加载的实现类
 * <p>之后，调用{@link com.huawei.apm.core.config.ConfigManager#initialize(java.util.Map)}初始化所有配置对象
 * <p>初始化之后，调用{@link com.huawei.apm.core.config.ConfigManager#getConfig(Class)}获取配置对象
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/8/19
 */
public interface BaseConfig {

}

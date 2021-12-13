/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */

package com.lubanops.stresstest.config;

/**
 * 配置工厂类
 *
 * @author yiwei
 * @since 2021/10/25
 */
public class ConfigFactory {
    /**
     * 获取配置类，当前只支持文件配置
     *
     * @return 配置类
     */
    public static Config getConfig() {
        return FileConfig.getInstance();
    }
}

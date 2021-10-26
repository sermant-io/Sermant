/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.premain.plugin;

import java.util.List;

/**
 * 插件配置类
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/8/24
 */
public class PluginConfig {
    private String pluginName;
    private List<InterceptorAlia> interceptors;

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public List<InterceptorAlia> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(List<InterceptorAlia> interceptors) {
        this.interceptors = interceptors;
    }

    /**
     * 拦截器别名类
     */
    public static class InterceptorAlia {
        private String name;
        private String alia;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAlia() {
            return alia;
        }

        public void setAlia(String alia) {
            this.alia = alia;
        }
    }
}

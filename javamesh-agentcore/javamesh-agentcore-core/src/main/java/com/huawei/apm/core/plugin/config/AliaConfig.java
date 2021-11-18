/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.plugin.config;

import java.util.List;

import com.huawei.apm.core.config.common.BaseConfig;

/**
 * 插件配置，于插件包中定义的统一配置的基类
 * <p>可以封装插件名称和别名信息
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/12
 */
public abstract class AliaConfig implements PluginConfig {
    /**
     * 插件名称
     */
    private String pluginName;

    /**
     * 拦截器别名
     */
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
     * 拦截器别名对象
     */
    public static class InterceptorAlia {
        /**
         * 拦截器全限定名
         */
        private String name;

        /**
         * 拦截器别名
         */
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

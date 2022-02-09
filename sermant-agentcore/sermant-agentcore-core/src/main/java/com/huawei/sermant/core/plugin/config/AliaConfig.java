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

package com.huawei.sermant.core.plugin.config;

import com.huawei.sermant.core.agent.annotations.AboutDelete;

import java.util.List;

/**
 * 插件配置，于插件包中定义的统一配置的基类
 * <p>可以封装插件名称和别名信息
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
@AboutDelete
@Deprecated
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

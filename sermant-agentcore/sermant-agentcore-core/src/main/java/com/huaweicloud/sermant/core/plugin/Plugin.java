/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.plugin;

import com.huaweicloud.sermant.core.plugin.classloader.PluginClassLoader;
import com.huaweicloud.sermant.core.plugin.classloader.ServiceClassLoader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于维护插件信息 插件名，插件包目录，插件服务，插件配置，插件主模块的类加载器，插件服务类加载器，插件的ResetTransformer
 *
 * @author luanwenfei
 * @since 2023-05-30
 */
public class Plugin {
    private String name;

    private String version;

    private String path;

    private List<String> serviceList = new ArrayList<>();

    private List<String> configList = new ArrayList<>();

    private PluginClassLoader pluginClassLoader;

    private ServiceClassLoader serviceClassLoader;

    /**
     * 构造方法
     *
     * @param name 插件名
     * @param path 插件路径
     * @param pluginClassLoader 插件类加载器
     */
    public Plugin(String name, String path, PluginClassLoader pluginClassLoader) {
        this.name = name;
        this.path = path;
        this.pluginClassLoader = pluginClassLoader;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<String> serviceList) {
        this.serviceList = serviceList;
    }

    public List<String> getConfigList() {
        return configList;
    }

    public void setConfigList(List<String> configList) {
        this.configList = configList;
    }

    public PluginClassLoader getPluginClassLoader() {
        return pluginClassLoader;
    }

    public void setPluginClassLoader(PluginClassLoader pluginClassLoader) {
        this.pluginClassLoader = pluginClassLoader;
    }

    public ServiceClassLoader getServiceClassLoader() {
        return serviceClassLoader;
    }

    public void setServiceClassLoader(ServiceClassLoader serviceClassLoader) {
        this.serviceClassLoader = serviceClassLoader;
    }

    /**
     * 构造插件服务类加载器
     *
     * @param urls 插件服务类加载器的搜索路径集合
     */
    public void createServiceClassLoader(URL[] urls) {
        if (urls.length > 0) {
            this.serviceClassLoader = new ServiceClassLoader(urls, this.pluginClassLoader);
        }
    }

    /**
     * 插件关闭
     *
     * @throws IOException IOException
     */
    public void close() throws IOException {
        if (serviceClassLoader != null) {
            serviceClassLoader.close();
        }

        if (pluginClassLoader != null) {
            pluginClassLoader.close();
        }
    }
}

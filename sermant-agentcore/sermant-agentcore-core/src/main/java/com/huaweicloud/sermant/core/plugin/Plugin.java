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

import net.bytebuddy.agent.builder.ResettableClassFileTransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用于维护插件信息 插件名，插件包目录，插件服务，插件配置，插件主模块的类加载器，插件服务类加载器，插件的ResetTransformer
 *
 * @author luanwenfei
 * @since 2023-05-30
 */
public class Plugin {
    /**
     * 插件名
     */
    private String name;

    /**
     * 插件版本
     */
    private String version;

    /**
     * 插件所在路径
     */
    private String path;

    /**
     * 动态插件, true 则可动态安装 false 则不可动态安装
     */
    private boolean isDynamic;

    /**
     * 插件服务列表
     */
    private List<String> services = new ArrayList<>();

    /**
     * 插件配置列表
     */
    private List<String> configs = new ArrayList<>();

    /**
     * 插件拦截器列表，需要通过adviceKey进行索引
     */
    private HashMap<String, Set<String>> interceptors = new HashMap<>();

    /**
     * 持有advice锁的adviceKey集合
     */
    private Set<String> adviceLocks = new HashSet<>();

    /**
     * 用于加载插件主模块的类加载器
     */
    private PluginClassLoader pluginClassLoader;

    /**
     * 用于加载插件服务模块的类加载器
     */
    private ServiceClassLoader serviceClassLoader;

    /**
     * 可重置的类文件转换器
     */
    private ResettableClassFileTransformer classFileTransformer;

    /**
     * 构造方法
     *
     * @param name 插件名
     * @param path 插件路径
     * @param isDynamic 插件是否为动态安装时加载
     * @param pluginClassLoader 插件类加载器
     */
    public Plugin(String name, String path, boolean isDynamic, PluginClassLoader pluginClassLoader) {
        this.name = name;
        this.path = path;
        this.isDynamic = isDynamic;
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

    public boolean isDynamic() {
        return isDynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.isDynamic = dynamic;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public List<String> getConfigs() {
        return configs;
    }

    public void setConfigs(List<String> configs) {
        this.configs = configs;
    }

    public HashMap<String, Set<String>> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(HashMap<String, Set<String>> interceptors) {
        this.interceptors = interceptors;
    }

    public Set<String> getAdviceLocks() {
        return adviceLocks;
    }

    public void setAdviceLocks(Set<String> adviceLocks) {
        this.adviceLocks = adviceLocks;
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

    public ResettableClassFileTransformer getClassFileTransformer() {
        return classFileTransformer;
    }

    public void setClassFileTransformer(ResettableClassFileTransformer classFileTransformer) {
        this.classFileTransformer = classFileTransformer;
    }
}

package com.huawei.apm.bootstrap.boot;

/**
 * 用于插件初始化
 */
public interface PluginService {
    void init();

    void stop();
}

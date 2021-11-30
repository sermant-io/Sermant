package com.huawei.flowcontrol.console.entity;

public interface RuleProvider<T> {
    T getRules(String appName) throws Exception;
}

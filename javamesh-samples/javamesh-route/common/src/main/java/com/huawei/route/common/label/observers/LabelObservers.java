/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.label.observers;

import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.bootstrap.lubanops.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * 标签观察者
 *
 * @author zhouss
 * @since 2021-07-01
 */
public enum LabelObservers {
    /**
     * 观察者单例
     */
    INSTANCE;

    private static final Logger LOGGER = LogFactory.getLogger();

    private final Map<String, List<LabelUpdateObserver>> observers;

    LabelObservers() {
        observers = new HashMap<String, List<LabelUpdateObserver>>();
    }

    /**
     * 观察者注册
     *
     * @param observer 观察者
     */
    public void registerLabelObservers(LabelUpdateObserver observer) {
        List<LabelUpdateObserver> observerList = observers.get(observer.getLabelName());
        if (observerList == null || observerList.isEmpty()) {
            observerList = new ArrayList<LabelUpdateObserver>();
        }
        observerList.add(observer);
        observers.put(observer.getLabelName(), observerList);
    }

    /**
     * 通知观察者更新自身配置
     *
     * @param labelName       标签名
     * @param labelProperties 更新内容
     */
    public void notifyAllObservers(String labelName, Properties labelProperties) {
        if (StringUtils.isBlank(labelName) || labelProperties == null) {
            return;
        }
        List<LabelUpdateObserver> observerList = observers.get(labelName);
        if (observerList == null) {
            return;
        }
        final Map<String, Properties> resolveProperties = resolveProperties(labelName, labelProperties);
        for (LabelUpdateObserver observer : observerList) {
            try {
                observer.notify(resolveProperties);
            } catch (Exception e) {
                LOGGER.warning(String.format("update label {%s}, class {%s} failed!",
                        labelName, observer.getClass().getName()));
            }
        }
    }

    /**
     * 兼容已经实现的接口，适配接口map参数，将properties转为map
     *
     * @param labelName  标签名
     * @param properties 属性
     * @return map
     */
    private Map<String, Properties> resolveProperties(String labelName, Properties properties) {
        Map<String, Properties> result = new HashMap<String, Properties>();
        result.put(labelName, properties);
        return result;
    }
}

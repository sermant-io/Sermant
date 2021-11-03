/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.bootstrap.lubanops.utils.StringUtils;
import com.huawei.route.common.label.observers.LabelProperties;
import com.huawei.route.report.common.entity.ServiceEssentialMessage;
import com.huawei.route.report.common.entity.ServiceRegisterMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 应用上报数据暂存
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-07-14
 */
public class ServiceRegisterCache {
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 实例对象
     */
    private static final ServiceRegisterCache SERVICE_REGISTER_CACHE = new ServiceRegisterCache();

    /**
     * 获取ldc信息的key
     */
    private final String LDC_KEY = "LDC_CONFIGURATION";

    /**
     * ldc的名称的key
     */
    private final String LDC_NAME_KEY = "ldc";

    /**
     * 标签配置的business的key
     */
    private final String LDC_BUSINESS_KEY = "businesses";

    /**
     * ldc数据的key
     */
    private final String LDC_VALUE_KEY = "value";

    /**
     * 未完整的缓存集合
     */
    private final List<ServiceRegisterMessage> serviceRegisterMessageList = new ArrayList<ServiceRegisterMessage>();

    /**
     * 完整的缓存集合
     */
    private final BlockingDeque<ServiceRegisterMessage> serviceRegisterMessageBlockingDeque =
            new LinkedBlockingDeque<ServiceRegisterMessage>();

    /**
     * 已经发送过注册数据的集合
     */
    private final Map<String, ServiceRegisterMessage> alreadySentRegisterMessageDeque =
            new HashMap<String, ServiceRegisterMessage>();

    /**
     * 获取服务注册缓存对象
     *
     * @return 缓存对象
     */
    public static ServiceRegisterCache getInstance() {
        return SERVICE_REGISTER_CACHE;
    }


    /**
     * 过滤数据同时补全数据
     */
    private void filterData() {
        Iterator<ServiceRegisterMessage> iterator = serviceRegisterMessageList.iterator();
        while (iterator.hasNext()) {
            ServiceRegisterMessage serviceRegisterMessage = iterator.next();
            assignment(serviceRegisterMessage);
            if (serviceRegisterMessageIsComplete(serviceRegisterMessage)) {
                offerServiceRegisterMessage(serviceRegisterMessage);
                iterator.remove();
            }
        }
    }

    /**
     * 添加包含下游信息的注册信息
     *
     * @param serviceRegisterMessage 注册信息
     */
    public void addServiceRegisterMessageByDownService(ServiceRegisterMessage serviceRegisterMessage) {
        LOGGER.finer(String.format(Locale.ENGLISH,
                "add service register message:{%s},this register message must has down service.",
                serviceRegisterMessage.toString()));
        if (StringUtils.isNotBlank(serviceRegisterMessage.getServiceName())) {
            ServiceEssentialMessageCache.getInstance().setServiceName(serviceRegisterMessage.getServiceName());
        }
        assignment(serviceRegisterMessage);
        if (serviceRegisterMessageIsComplete(serviceRegisterMessage)) {
            offerServiceRegisterMessage(serviceRegisterMessage);
            return;
        }
        serviceRegisterMessageList.add(serviceRegisterMessage);
    }

    /**
     * 给上报数据赋值
     *
     * @param serviceRegisterMessage 上报数据
     */
    private void assignment(ServiceRegisterMessage serviceRegisterMessage) {
        String clusterName = ServiceEssentialMessageCache.getInstance().getClusterName();
        if (StringUtils.isNotBlank(clusterName)) {
            serviceRegisterMessage.setClusterName(clusterName);
        }
        String registrarServiceName = ServiceEssentialMessageCache.getInstance().getRegistrarServiceName();
        if (StringUtils.isNotBlank(registrarServiceName)) {
            serviceRegisterMessage.setRegistrarServiceName(registrarServiceName);
        }
        String root = ServiceEssentialMessageCache.getInstance().getRoot();
        if (StringUtils.isNotBlank(root)) {
            serviceRegisterMessage.setRoot(root);
        }
        String serviceName = ServiceEssentialMessageCache.getInstance().getServiceName();
        if (StringUtils.isNotBlank(serviceName)) {
            serviceRegisterMessage.setServiceName(serviceName);
        }
        String protocol = ServiceEssentialMessageCache.getInstance().getProtocol();
        if (StringUtils.isNotBlank(protocol)) {
            serviceRegisterMessage.setProtocol(protocol);
        }
        String registry = ServiceEssentialMessageCache.getInstance().getRegistry();
        if (StringUtils.isNotBlank(registry)) {
            serviceRegisterMessage.setRegistry(registry);
        }

        Map<String, Properties> labelProperties = LabelProperties.getAllLabelProperties();
        if (!labelProperties.containsKey(LDC_KEY)) {
            return;
        }
        LOGGER.finer(String.format("ldc configuration message is:%s", labelProperties.get(LDC_KEY)));
        JSONObject ldcJson = JSON.parseObject(labelProperties.get(LDC_KEY).getProperty(LDC_VALUE_KEY));
        if (ldcJson != null) {
            String ldc = ldcJson.getString(LDC_NAME_KEY);
            JSONArray businessArray;
            try {
                businessArray = JSONArray.parseArray(ldcJson.getString(LDC_BUSINESS_KEY));
                serviceRegisterMessage.setLdc(ldc);
                serviceRegisterMessage.setBusinesses(businessArray);
            } catch (ClassCastException classCastException) {
                LOGGER.log(Level.WARNING, String.format(Locale.ENGLISH,
                        "ldc businesses label configuration error. ldc configuration is:%s",
                        ldcJson.toJSONString()), classCastException);
            }
        }
    }

    /**
     * 添加数据至完整队列
     *
     * @param serviceRegisterMessage 上报数据
     */
    public void offerServiceRegisterMessage(ServiceRegisterMessage serviceRegisterMessage) {
        DownServiceCache.getInstance().addDownService(serviceRegisterMessage.getDownServiceName());
        serviceRegisterMessageBlockingDeque.offer(serviceRegisterMessage);
    }

    /**
     * 需要发送的注册信息数量
     *
     * @return 数量
     */
    public int needSendMessageSize() {
        return serviceRegisterMessageBlockingDeque.size();
    }

    /**
     * 获取需要发送的注册信息
     *
     * @return 注册信息集
     */
    public Set<ServiceRegisterMessage> getServiceRegisterMessageList() {
        Set<ServiceRegisterMessage> list = new HashSet<ServiceRegisterMessage>();
        serviceRegisterMessageBlockingDeque.drainTo(list);
        return list;
    }

    /**
     * 添加缓存信息
     *
     * @param serviceEssentialMessage 缓存信息
     */
    public void addServiceRegisterMessage(ServiceEssentialMessage serviceEssentialMessage) {
        LOGGER.finer(String.format("add service register message:%s,this register message possible no down service.",
                serviceEssentialMessage.toString()));
        setServiceEssentialMessageCache(serviceEssentialMessage);
        if (serviceRegisterMessageList.isEmpty()) {
            return;
        }
        filterData();
    }

    /**
     * LDC的信息已经更新了
     */
    public void notifyLdcMessageComing() {
        LOGGER.finer("ldc label has change or first time run service!");
        Properties routeConfigProperties = LabelProperties.getAllLabelProperties().get(LDC_KEY);
        JSONObject ldcJson = JSON.parseObject(routeConfigProperties.getProperty(LDC_VALUE_KEY));

        // 信息已经补全但是发送失败并且ldc信息已经做了更新
        List<ServiceRegisterMessage> list = new ArrayList<ServiceRegisterMessage>(serviceRegisterMessageBlockingDeque.size());
        serviceRegisterMessageBlockingDeque.drainTo(list);
        for (ServiceRegisterMessage message : list) {
            message.setLdc(ldcJson.getString(LDC_NAME_KEY));
            message.setBusinesses(JSONArray.parseArray(ldcJson.getString(LDC_BUSINESS_KEY)));
            serviceRegisterMessageBlockingDeque.offer(message);
        }

        // 上报信息已经上报过，修改之后再一次上报
        for (Map.Entry<String, ServiceRegisterMessage> entry : alreadySentRegisterMessageDeque.entrySet()) {
            ServiceRegisterMessage message = entry.getValue();
            message.setLdc(ldcJson.getString(LDC_NAME_KEY));
            message.setBusinesses(JSONArray.parseArray(ldcJson.getString(LDC_BUSINESS_KEY)));
            offerServiceRegisterMessage(message);
        }

        // 信息未补全
        if (!serviceRegisterMessageList.isEmpty()) {
            for (ServiceRegisterMessage message : serviceRegisterMessageList) {
                message.setLdc(ldcJson.getString(LDC_NAME_KEY));
                message.setBusinesses(JSONArray.parseArray(ldcJson.getString(LDC_BUSINESS_KEY)));
            }
            filterData();
        }
    }

    /**
     * 添加完整的注册信息
     *
     * @param list 注册信息集合
     */
    public void addServiceRegisterMessage(Set<ServiceRegisterMessage> list) {
        serviceRegisterMessageBlockingDeque.addAll(list);
    }

    /**
     * 缓存已经发送成功的注册信息
     *
     * @param list 注册信息集合
     */
    public void addOldServiceRegisterMessage(Set<ServiceRegisterMessage> list) {
        for (ServiceRegisterMessage message : list) {
            if (!alreadySentRegisterMessageDeque.containsKey(message.getDownServiceName())) {
                alreadySentRegisterMessageDeque.put(message.getDownServiceName(), message);
            }
        }
    }

    /**
     * 设置缓存实体类的内容
     *
     * @param serviceEssentialMessageCache 缓存的实体类
     */
    private void setServiceEssentialMessageCache(ServiceEssentialMessage serviceEssentialMessageCache) {
        if (StringUtils.isNotBlank(serviceEssentialMessageCache.getClusterName())) {
            ServiceEssentialMessageCache.getInstance().setClusterName(serviceEssentialMessageCache.getClusterName());
        }

        if (StringUtils.isNotBlank(serviceEssentialMessageCache.getRoot())) {
            ServiceEssentialMessageCache.getInstance().setRoot(serviceEssentialMessageCache.getRoot());
        }

        if (StringUtils.isNotBlank(serviceEssentialMessageCache.getServiceName())) {
            ServiceEssentialMessageCache.getInstance().setServiceName(serviceEssentialMessageCache.getServiceName());
        }

        if (StringUtils.isNotBlank(serviceEssentialMessageCache.getRegistrarServiceName())) {
            ServiceEssentialMessageCache
                    .getInstance()
                    .setRegistrarServiceName(serviceEssentialMessageCache.getRegistrarServiceName());
        }

        if (StringUtils.isNotBlank(serviceEssentialMessageCache.getProtocol())) {
            ServiceEssentialMessageCache.getInstance().setProtocol(serviceEssentialMessageCache.getProtocol());
        }

        if (StringUtils.isNotBlank(serviceEssentialMessageCache.getRegistry())) {
            ServiceEssentialMessageCache.getInstance().setRegistry(serviceEssentialMessageCache.getRegistry());
        }
    }

    /**
     * 判断上报的应用数据是否完整
     *
     * @param serviceRegisterMessage 上报数据
     * @return 判断结果
     */
    private boolean serviceRegisterMessageIsComplete(ServiceRegisterMessage serviceRegisterMessage) {
        return StringUtils.isNotBlank(serviceRegisterMessage.getRoot())
                && StringUtils.isNotBlank(serviceRegisterMessage.getServiceName())
                && StringUtils.isNotBlank(serviceRegisterMessage.getDownServiceName());
    }
}

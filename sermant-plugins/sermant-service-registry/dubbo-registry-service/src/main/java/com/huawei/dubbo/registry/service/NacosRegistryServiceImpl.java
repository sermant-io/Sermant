/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.dubbo.registry.service;

import com.huawei.dubbo.registry.entity.NacosServiceName;
import com.huawei.dubbo.registry.listener.NacosAggregateListener;
import com.huawei.dubbo.registry.service.nacos.NacosRegistryService;
import com.huawei.dubbo.registry.utils.NacosInstanceManageUtil;
import com.huawei.dubbo.registry.utils.NamingServiceUtils;
import com.huawei.dubbo.registry.utils.ReflectUtils;
import com.huawei.registry.config.NacosRegisterConfig;
import com.huawei.registry.config.RegisterServiceCommonConfig;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.StringUtils;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * nacos注册中心注册服务
 *
 * @since 2022-10-25
 */
public class NacosRegistryServiceImpl implements NacosRegistryService {
    private static final int SERVICE_INTERFACE_INDEX = 1;

    private static final int SERVICE_NAME_COMPARE_LENGTH = 3;

    private static final String PROVIDER_SIDE = "provider";

    private static final String REGISTER_CONSUMER_URL_KEY = "register-consumer-url";

    private static final String INTERFACE_KEY = "interface";

    private static final String CATEGORY_KEY = "category";

    private static final String PROTOCOL_KEY = "protocol";

    private static final String PATH_KEY = "path";

    private static final String VERSION_KEY = "version";

    private static final String NAME_SEPARATOR = ":";

    private static final String DEFAULT_CATEGORY = "providers";

    private static final String SIDE_KEY = "side";

    private static final String CHECK_KEY = "check";

    private static final String STATUS_UP = "UP";

    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * key对应关系：第一个Object -> URL，第二个Object -> NotifyListener
     */
    private final Map<Object, Map<Object, NacosAggregateListener>> originToAggregateListener =
            new ConcurrentHashMap<>();

    /**
     * key对应关系：Object -> URL，String -> serviceName
     */
    private final Map<Object, Map<NacosAggregateListener, Map<String, EventListener>>> nacosListeners =
            new ConcurrentHashMap<>();

    private NamingService namingService;

    private NacosRegisterConfig nacosRegisterConfig;

    private Instance registryInstance;

    private final NacosServiceNotify nacosServiceNotify = new NacosServiceNotify();

    private RegisterServiceCommonConfig commonConfig;

    @Override
    public void doRegister(Object url) {
        try {
            if (PROVIDER_SIDE.equals(ReflectUtils.getParameter(url, SIDE_KEY))
                    || PROVIDER_SIDE.equals(ReflectUtils.getProtocol(url))
                    || !StringUtils.isBlank(ReflectUtils.getParameter(url, REGISTER_CONSUMER_URL_KEY))) {
                String serviceName = getLegacySubscribedServiceName(url);
                registryInstance = createInstance(url);
                namingService.registerInstance(serviceName, nacosRegisterConfig.getGroup(), registryInstance);
            }
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "register failed，url:{%s}，"
                    + "，cause: {%s}", url, e.getErrMsg()), e);
        }
    }

    @Override
    public void doSubscribe(Object url, Object notifyListener) {
        NacosAggregateListener nacosAggregateListener = new NacosAggregateListener(notifyListener);
        originToAggregateListener.computeIfAbsent(url,
                key -> new ConcurrentHashMap<>()).put(notifyListener, nacosAggregateListener);
        Set<String> serviceNames = getServiceNames(url);
        if (isServiceNamesWithCompatibleMode(url)) {
            for (String serviceName : serviceNames) {
                NacosInstanceManageUtil.setCorrespondingServiceNames(serviceName, serviceNames);
            }
        }
        doSubscribe(url, nacosAggregateListener, serviceNames);
    }

    private void doSubscribe(final Object url, final NacosAggregateListener listener, final Set<String> serviceNames) {
        try {
            if (isServiceNamesWithCompatibleMode(url)) {
                for (String serviceName : serviceNames) {
                    List<Instance> instances = namingService.getAllInstances(serviceName,
                            nacosRegisterConfig.getGroup());
                    NacosInstanceManageUtil.initOrRefreshServiceInstanceList(serviceName, instances);
                    notifySubscriber(url, serviceName, listener, instances);
                    subscribeEventListener(serviceName, url, listener);
                }
            } else {
                // 当url的serviceInterface、group中含“，”、“*”时，需要从新构建监听url
                // serviceName示例：providers:com.huawei.dubbo.registry.service.RegistryService:default,A
                for (String serviceName : serviceNames) {
                    String serviceInterface = serviceName;
                    String[] segments = serviceName.split(nacosRegisterConfig.getServiceNameSeparator(), -1);
                    if (segments.length == SERVICE_NAME_COMPARE_LENGTH) {
                        serviceInterface = segments[SERVICE_INTERFACE_INDEX];
                    }
                    Object subscriberUrl = ReflectUtils.setPath(url, serviceInterface);
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put(INTERFACE_KEY, serviceInterface);
                    parameters.put(CHECK_KEY, String.valueOf(false));
                    subscriberUrl = ReflectUtils.addParameters(subscriberUrl, parameters);
                    List<Instance> instances = new LinkedList<>(namingService.getAllInstances(serviceName,
                            nacosRegisterConfig.getGroup()));
                    notifySubscriber(subscriberUrl, serviceName, listener, instances);
                    subscribeEventListener(serviceName, subscriberUrl, listener);
                }
            }
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "failed to subscribe to nacos, url:{%s},"
                    + "cause:{%s}", url, e.getErrMsg()), e);
        }
    }

    private void subscribeEventListener(String serviceName, final Object url, final NacosAggregateListener listener)
            throws NacosException {
        Map<NacosAggregateListener, Map<String, EventListener>> listeners = nacosListeners.computeIfAbsent(url,
                key -> new ConcurrentHashMap<>());
        Map<String, EventListener> eventListeners = listeners.computeIfAbsent(listener,
                key -> new ConcurrentHashMap<>());
        EventListener eventListener = eventListeners.computeIfAbsent(serviceName,
                key -> new RegistryChildListenerImpl(serviceName, url, listener));
        namingService.subscribe(serviceName, nacosRegisterConfig.getGroup(), eventListener);
    }

    @Override
    public void doUnregister(Object url) {
        try {
            String serviceName = getLegacySubscribedServiceName(url);
            namingService.deregisterInstance(serviceName, nacosRegisterConfig.getGroup(), registryInstance);
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "failed to unregister，url: {%s}"
                    + "，cause: {%s}", url, e.getErrMsg()), e);
        }
    }

    @Override
    public void buildNamingService(Map<String, String> parameters) {
        nacosRegisterConfig = PluginConfigManager.getPluginConfig(NacosRegisterConfig.class);
        commonConfig = PluginConfigManager.getPluginConfig(RegisterServiceCommonConfig.class);
        this.namingService = NamingServiceUtils.buildNamingService(parameters, nacosRegisterConfig, commonConfig);
    }

    @Override
    public void doUnsubscribe(Object url, Object notifyListener) {
        Map<Object, NacosAggregateListener> listenerMap = originToAggregateListener.get(url);
        if (listenerMap == null) {
            LOGGER.warning(String.format(Locale.ENGLISH, "No aggregate listener found for url: {%s},",
                    url));
            return;
        }
        NacosAggregateListener nacosAggregateListener = listenerMap.remove(notifyListener);
        if (nacosAggregateListener != null) {
            Set<String> serviceNames = nacosAggregateListener.getServiceNames();
            try {
                doUnsubscribe(url, nacosAggregateListener, serviceNames);
                for (String serviceName : serviceNames) {
                    NacosInstanceManageUtil.removeCorrespondingServiceNames(serviceName);
                }
            } catch (NacosException e) {
                LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "Failed unsubscribe url: {%s}",
                        url), e);
            }
        }
        if (listenerMap.isEmpty()) {
            originToAggregateListener.remove(url);
        }
    }

    private void doUnsubscribe(final Object url, final NacosAggregateListener nacosAggregateListener,
            final Set<String> serviceNames) throws NacosException {
        for (String serviceName : serviceNames) {
            unsubscribeEventListener(serviceName, url, nacosAggregateListener);
        }
    }

    private void unsubscribeEventListener(String serviceName, final Object url,
            final NacosAggregateListener listener) throws NacosException {
        Map<NacosAggregateListener, Map<String, EventListener>> listenerToServiceEvent = nacosListeners.get(url);
        if (listenerToServiceEvent == null) {
            return;
        }
        Map<String, EventListener> serviceToEventMap = listenerToServiceEvent.get(listener);
        if (serviceToEventMap == null) {
            return;
        }
        EventListener eventListener = serviceToEventMap.remove(serviceName);
        if (eventListener == null) {
            return;
        }
        namingService.unsubscribe(serviceName, nacosRegisterConfig.getGroup(), eventListener);
        if (serviceToEventMap.isEmpty()) {
            listenerToServiceEvent.remove(listener);
        }
        if (listenerToServiceEvent.isEmpty()) {
            nacosListeners.remove(url);
        }
    }

    @Override
    public boolean isAvailable() {
        return STATUS_UP.equals(namingService.getServerStatus());
    }

    private Instance createInstance(Object url) {
        Map<String, String> metaData = new HashMap<>();
        String category = ReflectUtils.getParameter(url, CATEGORY_KEY);
        metaData.put(CATEGORY_KEY, category == null ? DEFAULT_CATEGORY : category);
        metaData.put(PROTOCOL_KEY, ReflectUtils.getProtocol(url));
        metaData.put(PATH_KEY, ReflectUtils.getPath(url));
        Object newUrl = ReflectUtils.addParameters(url, metaData);
        Instance instance = new Instance();
        instance.setIp(ReflectUtils.getHost(url));
        instance.setPort(ReflectUtils.getPort(url));
        instance.setMetadata(ReflectUtils.getParameters(newUrl));
        return instance;
    }

    private void notifySubscriber(Object url, String serviceName, NacosAggregateListener listener,
            List<Instance> instances) {
        listener.saveAndAggregateAllInstances(serviceName);
        List<Object> urls = buildUrls(instances);
        notify(url, listener.getNotifyListener(), urls);
    }

    private void notify(Object url, Object listener, List<Object> urls) {
        if (url == null) {
            throw new IllegalArgumentException("notify url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }
        try {
            nacosServiceNotify.doNotify(url, listener, urls);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "failed to notify addresses for subscribe "
                    + "url:{%s}, cause:{%s}", url, e.getMessage()), e);
        }
    }

    private List<Object> buildUrls(Collection<Instance> instances) {
        List<Object> urls = new LinkedList<>();
        if (instances != null && !instances.isEmpty()) {
            for (Instance instance : instances) {
                urls.add(buildUrl(instance));
            }
        }
        return urls;
    }

    private Object buildUrl(Instance instance) {
        Map<String, String> metadata = instance.getMetadata();
        String address = instance.getIp() + nacosRegisterConfig.getServiceNameSeparator() + instance.getPort();
        Object object = ReflectUtils.valueOf(address);
        object = ReflectUtils.setProtocol(object, metadata.get(PROTOCOL_KEY));
        object = ReflectUtils.setPath(object, metadata.get(PATH_KEY));
        object = ReflectUtils.setHost(object, instance.getIp());
        return ReflectUtils.addParameters(object, instance.getMetadata());
    }

    private boolean isServiceNamesWithCompatibleMode(Object url) {
        return new NacosServiceName(url).isValid();
    }

    private Set<String> filterServiceNames(NacosServiceName serviceName) {
        Set<String> serviceNames = new LinkedHashSet<>();
        try {
            serviceNames.addAll(namingService.getServicesOfServer(1, Integer.MAX_VALUE,
                            nacosRegisterConfig.getGroup()).getData()
                    .stream()
                    .filter(this::isConformRules)
                    .map(NacosServiceName::new)
                    .filter(serviceName::isCompatible)
                    .map(NacosServiceName::toString)
                    .collect(Collectors.toList()));
            return serviceNames;
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, String.format(Locale.ENGLISH, "filter serviceName failed"
                    + ", serviceName: {%s}，cause: {%s}", serviceName, e.getErrMsg()), e);
        }
        return serviceNames;
    }

    private Set<String> getServiceNames(Object url) {
        NacosServiceName serviceName = new NacosServiceName(url);

        final Set<String> serviceNames;

        if (serviceName.isValid()) {
            serviceNames = new LinkedHashSet<>();
            serviceNames.add(serviceName.toString());
            String legacySubscribedServiceName = getLegacySubscribedServiceName(url);
            if (!serviceName.toString().equals(legacySubscribedServiceName)) {
                serviceNames.add(legacySubscribedServiceName);
            }
        } else {
            serviceNames = filterServiceNames(serviceName);
        }
        return serviceNames;
    }

    private String getLegacySubscribedServiceName(Object url) {
        StringBuilder serviceNameBuilder = new StringBuilder(DEFAULT_CATEGORY);
        appendIfPresent(url, serviceNameBuilder, INTERFACE_KEY);
        appendIfPresent(url, serviceNameBuilder, VERSION_KEY);
        serviceNameBuilder.append(nacosRegisterConfig.getServiceNameSeparator())
                .append(nacosRegisterConfig.getGroup());
        return serviceNameBuilder.toString();
    }

    private void appendIfPresent(Object url, StringBuilder target, String parameterName) {
        String parameterValue = ReflectUtils.getParameter(url, parameterName);
        if (!StringUtils.isBlank(parameterValue)) {
            target.append(nacosRegisterConfig.getServiceNameSeparator()).append(parameterValue);
        }
    }

    private boolean isConformRules(String serviceName) {
        return serviceName.split(NAME_SEPARATOR, -1).length == SERVICE_NAME_COMPARE_LENGTH;
    }

    /**
     * 注册监听子类
     *
     * @since 2022-10-25
     */
    private class RegistryChildListenerImpl implements EventListener {
        private final RegistryNotifier notifier;

        private final String serviceName;

        private final Object consumerUrl;

        private final NacosAggregateListener listener;

        /**
         * 构造方法
         *
         * @param serviceName 服务名
         * @param url 客户端url
         * @param listener 监听器
         */
        protected RegistryChildListenerImpl(String serviceName, Object url, NacosAggregateListener listener) {
            this.serviceName = serviceName;
            this.consumerUrl = url;
            this.listener = listener;
            this.notifier = new RegistryNotifier(nacosRegisterConfig.getNotifyDelay()) {
                @Override
                protected void doNotify(Object rawAddresses) {
                    List<Instance> instances = (List<Instance>) rawAddresses;
                    if (isServiceNamesWithCompatibleMode(consumerUrl)) {
                        NacosInstanceManageUtil.initOrRefreshServiceInstanceList(serviceName, instances);
                        instances = NacosInstanceManageUtil.getAllCorrespondingServiceInstanceList(serviceName);
                    }
                    notifySubscriber(consumerUrl, serviceName, listener, instances);
                }
            };
        }

        @Override
        public void onEvent(Event event) {
            if (event instanceof NamingEvent) {
                NamingEvent namingEvent = (NamingEvent) event;
                notifier.notify(namingEvent.getInstances());
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            RegistryChildListenerImpl that = (RegistryChildListenerImpl) obj;
            return Objects.equals(serviceName, that.serviceName) && Objects.equals(consumerUrl, that.consumerUrl)
                    && Objects.equals(listener, that.listener);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serviceName, consumerUrl, listener);
        }
    }

    public Instance getRegistryInstance() {
        return registryInstance;
    }

    public Map<Object, Map<Object, NacosAggregateListener>> getOriginToAggregateListener() {
        return originToAggregateListener;
    }
}

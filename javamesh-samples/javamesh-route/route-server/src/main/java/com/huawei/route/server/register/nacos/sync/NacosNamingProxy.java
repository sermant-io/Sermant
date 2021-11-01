/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.register.nacos.sync;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.CommonParams;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.client.naming.net.NamingProxy;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.huawei.route.server.config.RouteServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

/**
 * 扩展{@link NamingProxy}
 * 修改查询时写死namespace的问题，增加查询可用服务的方法
 *
 * @author zhouss
 * @since 2021-10-28
 */
@Component
public class NacosNamingProxy extends NamingProxy {

    private final RouteServerProperties routeServerProperties;

    private final List<String> serverList;

    @Autowired
    public NacosNamingProxy(RouteServerProperties routeServerProperties) {
        super(null, null, routeServerProperties.getGray().getNacos().getUrl(), new Properties());
        this.routeServerProperties = routeServerProperties;
        this.serverList = Arrays.asList(routeServerProperties.getGray().getNacos().getUrl().split(","));
    }

    @Override
    public String reqApi(String api, Map<String, String> params, Map<String, String> body, List<String> servers, String method) throws NacosException {
        if (CollectionUtils.isEmpty(servers)) {
            throw new NacosException(NacosException.INVALID_PARAM, "no server available");
        }

        NacosException exception = new NacosException();
        Random random = new Random(System.currentTimeMillis());
        int index = random.nextInt(servers.size());

        for (int i = 0; i < servers.size(); i++) {
            String server = servers.get(index);
            try {
                return callServer(api, params, body, server, method);
            } catch (NacosException e) {
                exception = e;
                if (NAMING_LOGGER.isDebugEnabled()) {
                    NAMING_LOGGER.debug("request {} failed.", server, e);
                }
            }
            index = (index + 1) % servers.size();
        }

        NAMING_LOGGER.error("request: {} failed, servers: {}, code: {}, msg: {}", api, servers, exception.getErrCode(),
                exception.getErrMsg());

        throw new NacosException(exception.getErrCode(),
                "failed to req API:" + api + " after all servers(" + servers + ") tried: " + exception.getMessage());
    }

    /**
     * 查询所有健康服务
     *
     * @param namespace 命名空间
     * @return nacos服务名列表
     */
    public List<ServiceInfo> queryHealthServices(String namespace) {
        final Map<String, String> params = new HashMap<>();
        params.put("withInstances", "false");
        params.put("hasIpCount", "true");
        params.put(CommonParams.NAMESPACE_ID, namespace);
        int pageNo = 0;
        final int onceQueryMaxQuerySize = routeServerProperties.getGray().getNacos().getOnceQueryMaxQuerySize();
        final String catalogServiceUrl = routeServerProperties.getGray().getNacos().getCatalogServiceUrl();
        final List<ServiceInfo> services = new ArrayList<>();
        while (true) {
            try {
                params.put("pageNo", String.valueOf(pageNo++));
                params.put("pageSize", String.valueOf(onceQueryMaxQuerySize));
                final String result = reqApi(catalogServiceUrl, params, null, serverList, "GET");
                final ServiceHosts serviceHosts = JSONObject.parseObject(result, ServiceHosts.class);
                if (serviceHosts == null || serviceHosts.getServiceList() == null) {
                    return services;
                }
                services.addAll(serviceHosts.getServiceList());
                if (serviceHosts.getCount() <= onceQueryMaxQuerySize) {
                    return services;
                }
            } catch (NacosException e) {
                return services;
            }
        }
    }
}

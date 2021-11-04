/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.report.acquire;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.route.common.report.acquire.selector.UrlSelectorFacade;
import com.huawei.route.common.report.common.entity.DataSource;
import com.huawei.route.common.report.common.entity.HttpClientResult;
import com.huawei.route.common.report.common.entity.LDC;
import com.huawei.route.common.report.common.entity.Service;
import com.huawei.route.common.report.common.entity.TargetServiceAddress;
import com.huawei.route.common.report.common.entity.Type;
import com.huawei.route.common.report.common.utils.HttpClientUtils;
import com.huawei.route.common.report.routeservice.RouteService;
import com.huawei.route.common.report.routeservice.strategy.RouteStrategys;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * 目标地址获取类
 * 暂时去除应用路由相关内容，待后续应用路由合并再考虑适配
 *
 * @author wl
 * @since 2021-06-08
 */
public class TargetAddrAcquire {
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 服务端server的地址
     */
    private static final String ROUTE_SERVER_URL = "/getAddrList";

    /**
     * 获取服务状态
     */
    private static final String SERVICE_STATUS_URL = "/getStatus";

    /**
     * 提交数据源
     */
    private static final String SUBMIT_DATASOURCE_URL = "/submitDataSource";

    /**
     * 获取数据源
     */
    private static final String ACQUIRE_DATASOURCE_URL = "/getDataSource";

    /**
     * 注册信息上报
     */
    private static final String REPORT_REGISTRY_URL = "/register/v1/report/reportRegistry";

    /**
     * 查询实例列表
     */
    private static final String INSTANCE_LIST_URL = "/route/v1/instance/condition/list";

    /**
     * 异常信息上报
     */
    private static final String REPORT_EXCEPTION_URL = "/reportException";

    /**
     * 单例
     */
    private static TargetAddrAcquire INSTANCE;

    /**
     * 路由server服务地址
     * http://localhost:8090
     */
    private final List<String> serverList;

    private TargetAddrAcquire(String serverUrls) {
        this.serverList = Arrays.asList(serverUrls.split(","));
    }

    /**
     * 从server端获取有效地址列表
     *
     * @param ldcName         LDC
     * @param applicationName 目标服务名
     * @param isErrorService  判断是否时异常应用
     * @return 目标服务地址列表
     */
    public boolean acquireIsErrorApplication(String ldcName, String applicationName, boolean isErrorService)
            throws Exception {
        HashMap<String, String> hash = new HashMap<String, String>();
        hash.put("ldc", ldcName);
        hash.put("targetServiceName", applicationName);
        hash.put("isError", String.valueOf(isErrorService));
        HttpClientResult httpClientResult = HttpClientUtils.doGet(getRequestUrl(ROUTE_SERVER_URL), hash);
        String result = httpClientResult.getContent();

        return Boolean.parseBoolean(result);
    }

    /**
     * 向外提供的获取路由后的目标地址的方法，使用默认的轮询路由策略
     *
     * @param ldc               LDC
     * @param targetServiceName 目标服务地址
     * @param targetServiceType 目标服务地址类
     * @return 目标服务地址
     */
    public TargetServiceAddress getTargetAddr(LDC ldc, Type targetServiceType,
        String targetServiceName) {
        return getTargetAddr(ldc, targetServiceType, targetServiceName, RouteStrategys.ROUND_STRATEGY);
    }

    /**
     * 向外提供的获取路由后的目标地址的方法，带路由策略
     *
     * @param ldc               LDC
     * @param targetServiceName 目标服务地址
     * @param strategy          路由策略
     * @param targetServiceType 目标服务类型
     * @return 目标服务地址
     */
    public TargetServiceAddress getTargetAddr(LDC ldc, Type targetServiceType,
        String targetServiceName, RouteStrategys strategy) {
        if (targetServiceName == null || "".equals(targetServiceName)) {
            LOGGER.warning("targetServiceName is null or empty.");
            return null;
        }
        if (ldc == null) {
            LOGGER.warning("ldc is null");
            return null;
        }
        List<? extends TargetServiceAddress> addrList;
        try {
            addrList = acquireTargetAddr(ldc.getName(), targetServiceName);
        } catch (Exception e) {
            LOGGER.warning(String.format(Locale.ENGLISH, "An exception occurred at the requesting server:%s",
                    e.getMessage()));
            return null;
        }
        if (addrList == null || addrList.size() == 0) {
            return null;
        }
        return RouteService.getAddrByRoute(strategy.getStrategy(), addrList,
                targetServiceName);
    }

    /**
     * 从server端获取有效地址列表
     *
     * @param ldc               LDC
     * @param targetServiceName 目标服务名
     * @return 目标服务地址列表
     * @throws URISyntaxException 异常
     * @throws IOException        异常
     */
    public List<TargetServiceAddress> acquireTargetAddr(String ldc, String targetServiceName)
            throws URISyntaxException, IOException {
        HashMap<String, String> hash = new HashMap<String, String>();
        hash.put("ldc", ldc);
        hash.put("targetServiceName", targetServiceName);
        HttpClientResult httpClientResult = HttpClientUtils.doGet(getRequestUrl(ROUTE_SERVER_URL), hash);
        String result = httpClientResult.getContent();
        return JSONArray.parseArray(result, TargetServiceAddress.class);
    }

    /**
     * 从server端获取有效地址列表
     *
     * @param targetServiceName 目标服务名
     * @return 目标服务地址列表
     * @throws URISyntaxException 异常
     * @throws IOException        异常
     */
    public List<TargetServiceAddress> acquireTargetAddr(String targetServiceName)
            throws URISyntaxException, IOException {
        HashMap<String, String> hash = new HashMap<String, String>();
        hash.put("targetServiceName", targetServiceName);
        HttpClientResult httpClientResult = HttpClientUtils.doGet(getRequestUrl(ROUTE_SERVER_URL), hash);
        String result = httpClientResult.getContent();
        return JSONArray.parseArray(result, TargetServiceAddress.class);
    }

    /**
     * 获取服务状态
     *
     * @param ldc               单元名
     * @param targetServiceName 目标服务名
     * @return 服务状态
     * @throws URISyntaxException 异常
     * @throws IOException        异常
     */
    public Service.ServiceStatus acquireServiceStatus(String ldc, String targetServiceName)
            throws URISyntaxException, IOException {
        HashMap<String, String> hash = new HashMap<String, String>();
        hash.put("ldc", ldc);
        hash.put("targetServiceName", targetServiceName);
        HttpClientResult httpClientResult = HttpClientUtils.doGet(getRequestUrl(SERVICE_STATUS_URL), hash);
        String result = httpClientResult.getContent();
        return JSONArray.parseObject(result, Service.class).getServiceStatus();
    }

    /**
     * 数据源上报
     *
     * @param json 数据源信息
     * @throws IOException 异常
     */
    public boolean reportDatasource(String json) throws IOException {
        HttpClientResult httpClientResult = HttpClientUtils.doPost(getRequestUrl(SUBMIT_DATASOURCE_URL), json);
        return httpClientResult.getCode() == 200;
    }

    /**
     * 获取目标数据源
     *
     * @param ldc         单元名
     * @param serviceName 服务名
     * @return 数据源名称
     * @throws URISyntaxException 异常
     * @throws IOException        异常
     */
    public String getDataSourceName(String ldc, String serviceName) throws URISyntaxException, IOException {
        HashMap<String, String> hash = new HashMap<String, String>();
        hash.put("ldc", ldc);
        hash.put("serviceName", serviceName);
        HttpClientResult httpClientResult = HttpClientUtils.doGet(getRequestUrl(ACQUIRE_DATASOURCE_URL), hash);
        String result = httpClientResult.getContent();
        DataSource dataSource = JSONObject.parseObject(result, DataSource.class);
        return dataSource.getName();
    }

    /**
     * 查询实例列表
     *
     * @param json 请求参数json串
     * @return HttpClientResult
     */
    public HttpClientResult queryInstanceList(String json) throws IOException {
        return HttpClientUtils.doPost(getRequestUrl(INSTANCE_LIST_URL), json);
    }

    /**
     * 注册信息上报
     *
     * @param json 注册信息
     * @return 上报是否成功
     * @throws IOException 异常
     */
    public boolean reportRegistryInfo(String json) throws IOException {
        HttpClientResult httpClientResult = HttpClientUtils.doPost(getRequestUrl(REPORT_REGISTRY_URL), json);
        return httpClientResult.getCode() == 200;
    }

    /**
     * 异常信息上报
     *
     * @param json 异常信息
     * @return 上报是否成功
     * @throws IOException 异常
     */
    public boolean reportExceptionInfo(String json) throws IOException {
        HttpClientResult httpClientResult = HttpClientUtils.doPost(getRequestUrl(REPORT_EXCEPTION_URL), json);
        return httpClientResult.getCode() == 200;
    }

    /**
     * 初始化
     * 通过插件初始化 report-plugin, 提供所有的路由模块使用
     *
     * @param serviceUrls 服务地址，逗号隔开
     */
    public static synchronized void initAcquire(String serviceUrls) {
        if (StringUtils.isBlank(serviceUrls)) {
            return;
        }
        INSTANCE = new TargetAddrAcquire(serviceUrls);
    }

    /**
     * 获取当前单例
     *
     * @return 单例
     */
    public static TargetAddrAcquire getInstance() {
        return INSTANCE;
    }

    private String getRequestUrl(String api) {
        final String selectUrl = UrlSelectorFacade.select(serverList);
        return selectUrl + api;
    }
}

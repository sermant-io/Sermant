/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 路由Server配置类
 *
 * @author zhouss
 * @since 2021-10-18
 */
@ConfigurationProperties(prefix = "route.server")
@Component
@Setter
@Getter
public class RouteServerProperties {

    private final GrayConfiguration gray = new GrayConfiguration();

    @Getter
    @Setter
    public static class GrayConfiguration {
        /**
         * 配置中心类型
         */
        private String configCenter = "zookeeper";

        /**
         * 注册中心类型
         */
        private String registerType = "nacos";

        /**
         * 路由标签（应用路由+灰度规则配置）
         */
        private String routeGroupName = "ROUTE_CONFIGURATION";

        /**
         * 灰度规则标签配置
         */
        private String grayLabelName = "GRAY_CONFIGURATION";

        /**
         * LDC 标签名称
         */
        private String ldcLabelName = "LDC_CONFIGURATION";

        /**
         * 标签同步间隔
         */
        private int tagSyncIntervalMs = 10000;

        /**
         * zookeeper配置中心配置
         */
        private final ZookeeperConfiguration zookeeper = new ZookeeperConfiguration();

        /**
         * nacos注册中心配置
         */
        private final NacosConfiguration nacos = new NacosConfiguration();

    }

    /**
     * zookeeper配置中心配置
     */
    @Getter
    @Setter
    public static class ZookeeperConfiguration {
        /**
         * 全局规则监听路径
         */
        private String configurationPath = "/general-paas/configurations/grayGlobalRule";
    }

    /**
     * nacos注册中心配置
     */
    @Getter
    @Setter
    public static class NacosConfiguration {
        /**
         * nacos注册中心地址
         */
        private String url = "http://localhost:8848";

        /**
         * nacos查询分组
         */
        private String groupName = "DEFAULT_GROUP";

        /**
         * 自定义的命名空间,多个命名空间使用逗号隔开
         * 格式: namespace1,namespace2
         */
        private String customNamespaceGroup = "";

        /**
         * nacos查询命名空间
         */
        private String namespaceId = "public";

        /**
         * 单次最大查询服务数
         */
        private int onceQueryMaxQuerySize = 1000;

        /**
         * 查询所有服务地址  get
         * =========请求参数========
         * 名称	类型	是否必选	描述
         * pageNo	int	是	当前页码
         * pageSize	int	是	分页大小
         * groupName	字符串	否	分组名
         * namespaceId	字符串	否	命名空间ID
         * ============错误编码==========
         * 错误代码	描述	语义
         * 400	Bad Request	客户端请求中的语法错误
         * 403	Forbidden	没有权限
         * 404	Not Found	无法找到资源
         * 500	Internal Server Error	服务器内部错误
         * 200	OK	正常
         * ========返回数据=========
         * {
         * "count":148,
         * "doms": [
         * "nacos.test.1",
         * "nacos.test.2"
         * ]
         * }
         */
        private String serviceUrl = "nacos/v1/ns/service/list";

        /**
         * 查询所有实例地址  get
         * <p>
         * ===========请求参数============
         * 名称	类型	是否必选	描述
         * serviceName	字符串	是	服务名
         * groupName	字符串	否	分组名
         * namespaceId	字符串	否	命名空间ID
         * clusters	字符串，多个集群用逗号分隔	否	集群名称
         * healthyOnly	boolean	否，默认为false	是否只返回健康实例
         * 错误编码
         * 错误代码	描述	语义
         * 400	Bad Request	客户端请求中的语法错误
         * 403	Forbidden	没有权限
         * 404	Not Found	无法找到资源
         * 500	Internal Server Error	服务器内部错误
         * 200	OK	正常
         * ============返回数据============
         * {
         * "dom": "nacos.test.1",
         * "cacheMillis": 1000,
         * "useSpecifiedURL": false,
         * "hosts": [{
         * "valid": true,
         * "marked": false,
         * "instanceId": "10.10.10.10-8888-DEFAULT-nacos.test.1",
         * "port": 8888,
         * "ip": "10.10.10.10",
         * "weight": 1.0,
         * "metadata": {}
         * }],
         * "checksum": "3bbcf6dd1175203a8afdade0e77a27cd1528787794594",
         * "lastRefTime": 1528787794594,
         * "env": "",
         * "clusters": ""
         * }
         */
        private String serviceInstanceUrl = "nacos/v1/ns/instance/list";

        /**
         * 分类请求地址
         */
        private String catalogServiceUrl = "/nacos/v1/ns/catalog/services";

        /**
         * nacos服务名分隔符
         */
        private String serviceSeparator = ":";
    }

}

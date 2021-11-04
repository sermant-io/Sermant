/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.constants;

import com.huawei.route.server.config.RouteServerProperties;

/**
 * nacos的相关常量
 *
 * @author zhouss
 * @since 2021-10-26
 */
public class NacosConstants {
    /**
     * dubbo服务名
     */
    public static final String APPLICATION = "application";

    /**
     * nacos集群名
     */
    public static final String CLUSTER_NAME = "clusterName";
    /**
     * nacos集群名 命名空间
     */
    public static final String NAMESPACE = "namespace";


    /**
     * nacos分组名
     */
    public static final String GROUP = "group";

    /**
     * nacos版本名
     */
    public static final String VERSION = "version";

    /**
     * nacos dubbo应用的消费端
     */
    public static final String SIDE = "side";

    /**
     * 针对dubbo含有接口分隔后的数组长度，数组的最后一个元素为版本号
     */
    public static final int INTERFACE_PARTS_LEN = 3;

    /**
     * 实例相关信息的分隔长度  通常以@@作为分隔符分为两部分
     * 第一部分：主要是实例的信息，例如端口、分组、ip
     * 第二部分：接口的相关信息，接口权限定、版本
     */
    public static final int INFO_PARTS_LEN = 2;

    /**
     * 实例ID分隔符，分为两部分，同上描述
     */
    public static final String NACOS_INSTANCE_ID_SEPARATOR = "@@";

    /**
     * nacos信息内部分隔符
     * 10.207.0.164#20880#DEFAULT#DEFAULT_GROUP
     */
    public static final char INFO_INNER_SEPARATOR = '#';

    /**
     * 默认命名空间
     */
    public static final String DEFAULT_NACOS_NAMESPACE = "public";

    /**
     * nacos自定义分组,组间分隔符
     * {@link RouteServerProperties.NacosConfiguration} 的属性customNamespaceGroup
     */
    public static final char CUSTOM_NACOS_NAMESPACE_SEPARATOR = ',';

    /**
     * nacos 分组内部 命名空间与组的分隔符
     * {@link RouteServerProperties.NacosConfiguration} 的属性customNamespaceGroup
     */
    public static final char CUSTOM_NACOS_NAMESPACE_GROUP_SEPARATOR = ':';

    /**
     * nacos 分组内部 组间的间隔
     * {@link RouteServerProperties.NacosConfiguration} 的属性customNamespaceGroup
     */
    public static final char CUSTOM_NACOS_GROUP_INNER_SEPARATOR = '|';
}

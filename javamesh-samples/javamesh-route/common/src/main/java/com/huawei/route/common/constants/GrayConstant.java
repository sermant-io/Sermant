package com.huawei.route.common.constants;

/**
 * 常量
 *
 * @author pengyuyi
 * @date 2021/10/13
 */
public class GrayConstant {
    public static final String GRAY_CONFIGURATION = "GRAY_CONFIGURATION";

    public static final String LABEL_VALUE = "value"; // 标签配置中，包含配置内容的字段

    public static final String LABEL_SWITCH_FILED_NAME = "on"; // 标签生效开关

    public static final String DUBBO_SOURCE_TYPE_PREFIX = "args"; // dubbo参数索引的前缀

    public static final String GRAY_LDC = "GRAY_LDC"; // 地域属性，属于哪个机房

    public static final String GRAY_TAG = "GRAY_TAG"; // 上游携带标签

    public static final String GRAY_DEFAULT_LDC = "DEFAULT_LDC"; // 灰度发布默认ldc

    public static final String GRAY_DEFAULT_VERSION = "DEFAULT_VERSION"; // 灰度发布默认版本

    public static final String QUERY_SERVICE_ADDR_KEY = "serviceNames"; // 查询实例地址的服务名的key

    public static final String QUERY_SERVICE_ADDR_THREAD_NAME = "query-addr-task"; // 查询地址的线程名

    public static final String ENABLED_METHOD_NAME = ".isEnabled()"; // isEnabled匹配的方法名

    public static final String URL_VERSION_KEY = "version"; // dubbo的版本字段

    public static final String URL_GROUP_KEY = "group"; // dubbo的分组字段

    public static final String URL_CLUSTER_NAME_KEY = "clusterName"; // dubbo的集群名字段

}

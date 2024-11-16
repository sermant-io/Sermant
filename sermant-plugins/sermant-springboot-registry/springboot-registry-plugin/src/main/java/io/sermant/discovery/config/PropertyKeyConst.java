package io.sermant.discovery.config;

/**
 * nacos注册插件配置
 *
 * @author xiaozhao
 * @since 2024-11-16
 */
public class PropertyKeyConst {
    /**
     * 冒号
     */
    public static final String HTTP_URL_COLON = ":";

    /**
     * 节点
     */
    public static final String ENDPOINT = "endpoint";

    /**
     * 节点端口
     */
    public static final String ENDPOINT_PORT = "endpointPort";

    /**
     * 命名空间
     */
    public static final String NAMESPACE = "namespace";

    /**
     * 用户名
     */
    public static final String USERNAME = "username";

    /**
     * 用户密码
     */
    public static final String PASSWORD = "password";

    /**
     * ak值
     */
    public static final String ACCESS_KEY = "accessKey";

    /**
     * sk值
     */
    public static final String SECRET_KEY = "secretKey";

    /**
     * 服务地址
     */
    public static final String SERVER_ADDR = "serverAddr";

    /**
     * 集群名称
     */
    public static final String CLUSTER_NAME = "clusterName";

    /**
     * 开始是否naming加载缓存
     */
    public static final String NAMING_LOAD_CACHE_AT_START = "namingLoadCacheAtStart";

    /**
     * nacos日志文件名
     */
    public static final String NACOS_NAMING_LOG_NAME = "com.alibaba.nacos.naming.log.filename";

    /**
     * 构造方法
     */
    private PropertyKeyConst() {
    }
}

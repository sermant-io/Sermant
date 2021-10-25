/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.oap.server.configuration.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * 授权信息生成工具
 *
 * @author zhouss
 * @since 2021-04-13
 **/
@Slf4j
public class ZKHelper {

    /**
     * 默认的scheme
     */
    public static final String DEFAULT_SCHEME = "pdzk";

    private static final String CONNECTOR_CHAR = ":";

    private static final String DEFAULT_CONFIG_FILE_PATH = "/opt/agent/zookeeper/zk.client.properties";

    private static final String CLIENT_KEYSTORE_PATH_KEY = "zookeeper.ssl.client.keyStore.path";

    private static final String CLIENT_TRUSTSTORE_PATH_KEY = "zookeeper.ssl.client.trustStore.path";

    private static final String CONFIG_FILE_KEY = "zookeeper.config.file.path";

    /**
     * 是否开启acl true or false
     */
    private static final String ACL_ENABLED = "acl.enabled";

    /**
     * 是否开启ssl
     */
    private static final String SSL_ENABLED = "ssl.enabled";

    private static final String ZK_CLIENT_CONFIG_PATH =
        PropertyUtil.getSystemEnv(CONFIG_FILE_KEY, DEFAULT_CONFIG_FILE_PATH);

    private static Properties properties = null;

    static {
        if (isEnabledSSL() || isEnabledACL()) {
            checkConfig();
        }
    }

    /**
     * 开启客户端SSL配置
     */
    public static void enableClientSSLConfig() {
        checkConfig();
        // 环境变量优先
        String keyStoreLocation =
            PropertyUtil.getSystemEnv(CLIENT_KEYSTORE_PATH_KEY, properties.getProperty(CLIENT_KEYSTORE_PATH_KEY));
        String trustStoreLocation =
            PropertyUtil.getSystemEnv(CLIENT_TRUSTSTORE_PATH_KEY, properties.getProperty(CLIENT_TRUSTSTORE_PATH_KEY));
        String keyStorePassword = properties.getProperty("zookeeper.ssl.keyStore.password");
        String trustStorePassword = properties.getProperty("zookeeper.ssl.trustStore.password");
        if (PropertyUtil.isBlank(keyStoreLocation) || PropertyUtil.isBlank(trustStoreLocation) || PropertyUtil.isBlank(
            keyStorePassword) || PropertyUtil.isBlank(trustStorePassword)) {
            throw new IllegalArgumentException("verified ssl params failed! please check your zk client config");
        }
        System.setProperty("zookeeper.clientCnxnSocket", "org.apache.zookeeper.ClientCnxnSocketNetty");
        System.setProperty("zookeeper.client.secure", "true");
        System.setProperty("zookeeper.ssl.keyStore.location", keyStoreLocation);
        System.setProperty("zookeeper.ssl.keyStore.password", Base64Utils.decode(keyStorePassword));
        System.setProperty("zookeeper.ssl.trustStore.location", trustStoreLocation);
        System.setProperty("zookeeper.ssl.trustStore.password", Base64Utils.decode(trustStorePassword));
        System.setProperty("zookeeper.ssl.hostnameVerification", "false");
        System.setProperty("zookeeper.ssl.protocols", "TLSv1.2");
        System.setProperty("zookeeper.ssl.trustStoreType", "JKS");
        System.setProperty("zookeeper.ssl.keyStoreType", "JKS");
    }

    /**
     * 获取zk客户端
     *
     * @param connectionTimeoutMs 连接超时时间
     * @param sessionTimeoutMs    zk会话超时时间
     * @param connectString       连接地址
     * @param retryPolicy         重试策略
     * @return zkClient
     */
    public static CuratorFramework getAclZkClient(int connectionTimeoutMs, int sessionTimeoutMs, String connectString,
        RetryPolicy retryPolicy) {
        final CuratorFrameworkFactory.Builder builder =
            CuratorFrameworkFactory.builder().retryPolicy(retryPolicy).connectString(connectString);
        if (isEnabledACL()) {
            log.info("enabled agent ACL");
            builder.aclProvider(new ACLProvider() {
                @Override
                public List<ACL> getDefaultAcl() {
                    return ZKHelper.getAclList();
                }

                @Override
                public List<ACL> getAclForPath(String path) {
                    return ZKHelper.getAclList();
                }
            }).authorization(DEFAULT_SCHEME, getAuthInfo().getBytes());
        }
        if (isEnabledSSL()) {
            log.info("enabled agent SSL");
            enableClientSSLConfig();
        }
        if (connectionTimeoutMs > 0) {
            builder.connectionTimeoutMs(connectionTimeoutMs);
        }
        if (sessionTimeoutMs > 0) {
            builder.sessionTimeoutMs(sessionTimeoutMs);
        }
        return builder.build();
    }

    /**
     * 获取zk客户端, 默认超时时间配置
     *
     * @param connectString 连接地址
     * @param retryPolicy   重试策略
     * @return zkClient
     */
    public static CuratorFramework getAclZkClient(String connectString, RetryPolicy retryPolicy) {
        return getAclZkClient(0, 0, connectString, retryPolicy);
    }

    /**
     * 通过文件拿取配置文件
     * 读取相关配置密码
     *
     * @return 授权信息
     */
    public static String getAuthInfo() {
        checkConfig();
        return getAuthInfo(properties.getProperty("username"),
            properties.getProperty("password"),
            properties.getProperty("salt"));
    }

    /**
     * 获取授权信息列表
     *
     * @return 授权列表
     */
    public static List<AuthInfo> getAuthInfoList() {
        return Arrays.asList(new AuthInfo(DEFAULT_SCHEME, getAuthInfo().getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 生成ACL授权信息
     *
     * @param userName 用户名
     * @param password 密码
     * @param salt     盐值
     * @return userName:BASE64(password):timestamp
     */
    public static String getAuthInfo(String userName, String password, String salt) {
        return new StringBuilder(userName).append(CONNECTOR_CHAR)
            .append(password)
            .append(CONNECTOR_CHAR)
            .append(System.currentTimeMillis())
            .toString();
    }

    /**
     * 生成ACL授权信息
     *
     * @param userName 用户名
     * @param password 密码
     * @param salt     盐值
     * @return bytes(userName : BASE64 ( password):timestamp)
     */
    public static byte[] getByteAuthInfo(String userName, String password, String salt) {
        return getAuthInfo(userName, password, salt).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 获取Acl
     *
     * @return List<ACL>
     */
    public static List<ACL> getAclList() {
        return Arrays.asList(new ACL(ZooDefs.Perms.ALL, new Id(DEFAULT_SCHEME, getAuthInfo())));
    }

    /**
     * 获取认证授权信息
     *
     * @param authInfo 授权信息 {@link ZKHelper#getAuthInfo}
     * @return acl list
     */
    public static List<ACL> getAclList(String authInfo) {
        return Arrays.asList(new ACL(ZooDefs.Perms.ALL, new Id(DEFAULT_SCHEME, authInfo)));
    }

    /**
     * 根据指定用户名，盐值生成Acls
     *
     * @param userName 用户名称
     * @param password 密码
     * @param salt     盐
     * @return List<ACL>
     */
    public static List<ACL> getAclList(String userName, String password, String salt) {
        return Arrays.asList(new ACL(ZooDefs.Perms.ALL, new Id(DEFAULT_SCHEME, getAuthInfo(userName, password, salt))));
    }

    private static String handlePassword(String password, String salt) {
        if (PropertyUtil.isBlank(salt)) {
            return Base64Utils.encode(password);
        }
        return Base64Utils.encode(PBKDF2Util.getEncryptedPassword(password, salt));
    }

    /**
     * 读取配置
     *
     * @return 文件配置
     */
    private static Properties readConfig() {
        final File file = new File(ZK_CLIENT_CONFIG_PATH);
        final Properties config = new Properties();
        if (!file.exists() || file.isDirectory()) {
            log.error(String.format(Locale.ENGLISH, "%s is not exist!", ZK_CLIENT_CONFIG_PATH));
            throw new IllegalArgumentException("zk acl configuration file not exist!");
        }
        readPlainConfig(config, file);
        return config;
    }

    private static void readPlainConfig(Properties config, File file) {
        try (InputStream is = FileCheckUtils.getFileInputStream(file)) {
            config.load(is);
        } catch (IOException e) {
            throw new IllegalArgumentException("read zk acl configuration file failed, please check the file path!");
        }
    }

    private static void checkConfig() {
        if (properties == null) {
            properties = readConfig();
        }
    }

    /**
     * 判断是否开启ACL权限控制
     *
     * @return boolean
     */
    public static boolean isEnabledACL() {
        return "true".equalsIgnoreCase(PropertyUtil.getSystemEnv(ACL_ENABLED, "false"));
    }

    /**
     * 判断是否开启SSL
     *
     * @return boolean
     */
    public static boolean isEnabledSSL() {
        return "true".equalsIgnoreCase(PropertyUtil.getSystemEnv(SSL_ENABLED, "false"));
    }
}

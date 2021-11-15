/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec.session;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 管理远程执行时，与不同服务器之间产生的session
 *
 * @author y30010171
 * @since 2021-10-20
 **/
@Component
public class ServerSessionFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerSessionFactory.class);
    private Map<ServerInfo, Session> sessionCache = new ConcurrentHashMap<>();
    private JSch jsch = new JSch();

    @Value("${jsch.connectTimeout}")
    private int connectTimeout;

    @Value("${jsch.knownHosts}")
    private String knownHosts;

    @Value("${jsch.privateKey}")
    private String privateKey;

    @PostConstruct
    public void init() {
        try {
            JSch.setLogger(new MyLogger());
            jsch.addIdentity(privateKey);
            jsch.setKnownHosts(knownHosts);
            this.LOGGER.info("set privateKey = {}, knownHosts = {}",
                privateKey, knownHosts);
        } catch (JSchException e) {
            this.LOGGER.error("Failed to set privateKey = {}, knownHosts = {}.{}",
                privateKey, knownHosts, e.getMessage());
        }
    }

    /**
     * 与远程服务器建立ssh连接
     *
     * @param serverInfo {@link ServerInfo} 服务器信息
     * @return {@link Session} 连接实例
     * @throws JSchException
     */
    public Session getSession(ServerInfo serverInfo) throws JSchException {
        Session session = sessionCache.getOrDefault(serverInfo, createSession(serverInfo));
        if (!session.isConnected()) {
            long startConnect = System.currentTimeMillis();
            session.connect(connectTimeout);
            LOGGER.info("connect to server {}:{} cost {} ms",
                    session.getHost(), session.getPort(), System.currentTimeMillis() - startConnect);
        }
        return session;
    }

    private Session createSession(ServerInfo serverInfo) throws JSchException {
        Session session =
                jsch.getSession(serverInfo.getServerUser(), serverInfo.getServerIp(), serverInfo.getServerPort());
        session.setConfig("StrictHostKeyChecking", "no");
        for (HostKey key : jsch.getHostKeyRepository().getHostKey()) {
            if (key.getHost().equals(serverInfo.getServerIp())) {
                LOGGER.info("set server_host_key = {}", key.getType());
                session.setConfig("server_host_key", key.getType());
                break;
            }
        }
        if (StringUtils.isNotEmpty(serverInfo.getServerPassword())) {
            session.setPassword(serverInfo.getServerPassword());
            session.setConfig("PreferredAuthentications", "password");
            LOGGER.info("connect to {}:{} set PreferredAuthentications => password", serverInfo.getServerIp(), serverInfo.getServerPort());
        }
        return session;
    }

    @PreDestroy
    public void close() {
        sessionCache.values().forEach(Session::disconnect);
        LOGGER.info("All server session closed.");
    }

    /**
     * jsch的日志输出
     *
     * @author y30010171
     * @since 2021-10-20
     **/
    static class MyLogger implements com.jcraft.jsch.Logger {
        @Override
        public boolean isEnabled(int level) {
            return true;
        }

        @Override
        public void log(int level, String message) {
            switch (level) {
                case DEBUG:
                    LOGGER.debug(message);
                    break;
                case INFO:
                    LOGGER.info(message);
                    break;
                case WARN:
                    LOGGER.warn(message);
                    break;
                case ERROR:
                    LOGGER.error(message);
                    break;
                case FATAL:
                    LOGGER.error("fatal info: {}", message);
                    break;
                default:
                    break;
            }
        }
    }
}

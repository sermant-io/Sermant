/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.databasepeerparse.service;

import com.huawei.apm.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.javamesh.sample.monitor.common.service.DatabasePeerParseService;
import org.apache.skywalking.apm.plugin.jdbc.connectionurl.parser.URLParser;
import org.apache.skywalking.apm.plugin.jdbc.trace.ConnectionInfo;

/**
 * Database peer 解析服务实现类
 */
public class DatabasePeerParseServiceImpl implements DatabasePeerParseService {

    @Override
    public String parse(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        ConnectionInfo connectionInfo;
        try {
            connectionInfo = URLParser.parser(url);
        } catch (Exception e) {
            // URLParser 有空指针异常BUG
            return null;
        }
        return connectionInfo == null ? null : connectionInfo.getDatabasePeer();
    }
}

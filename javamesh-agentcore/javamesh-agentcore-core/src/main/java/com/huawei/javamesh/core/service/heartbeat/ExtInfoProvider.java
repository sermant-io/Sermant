/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.core.service.heartbeat;

import java.util.Map;

/**
 * 信息额外信息提供者，当心跳发送的内容会发生改变时，需要定制信息提供方式
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/29
 */
public interface ExtInfoProvider {
    /**
     * 提供额外信息集合
     *
     * @return 额外信息集合
     */
    Map<String, String> getExtInfo();
}

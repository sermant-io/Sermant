/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.share;


import com.huawei.route.server.constants.RouteConstants;
import com.huawei.route.server.rules.notifier.PathDataUpdater;

import java.util.Collection;

/**
 * 服务数据共享实现, 目前未使用数据共享
 *
 * @author zhouss
 * @since 2021-10-18
 */
public abstract class RouteSharer<T> {
    /**
     * 数据分享键
     */
    protected static final String SHARE_KEY = "ROUTE_SERVER_SHARE";

    protected RouteSharer(PathDataUpdater pathDataUpdater) {
        this.pathDataUpdater = pathDataUpdater;
    }

    private final PathDataUpdater pathDataUpdater;

    /**
     * 数据共享
     *
     * @param data 共享数据
     * @return 是否共享成功
     */
    public boolean share(T[] data) {
        if (shareAllData(data)) {
            pathDataUpdater.updatePathData(RouteConstants.SHARE_NOTIFIER_PATH, null);
            return true;
        }
        return false;
    }

    /**
     * 分享数据
     *
     * @param data 目标数据
     * @return 是否存储成功
     */
    abstract boolean shareAllData(T[] data);

    /**
     * 从共享端获取共享数据
     *
     * @param tClass 目标类型
     * @return 共享数据
     */
    public abstract Collection<T> getShareDataList(Class<T> tClass);
}

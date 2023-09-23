/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.sermant.god.common;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理服务中已安装的Sermant
 *
 * @author luanwenfei
 * @since 2023-05-24
 */
public class SermantManager {
    private static final Map<String, SermantClassLoader> SERMANT_MANAGE_MAP = new ConcurrentHashMap<>();

    private static final Map<String, Boolean> SERMANT_STATUS = new ConcurrentHashMap<>();

    private SermantManager() {
    }

    /**
     * 创建Sermant
     *
     * @param artifact 标识Sermant
     * @param urls Sermant资源路径
     * @return SermantClassLoader
     */
    public static SermantClassLoader createSermant(String artifact, URL[] urls) {
        if (hasSermant(artifact)) {
            return SERMANT_MANAGE_MAP.get(artifact);
        }
        SermantClassLoader sermantClassLoader = new SermantClassLoader(urls);
        SERMANT_MANAGE_MAP.put(artifact, sermantClassLoader);
        return sermantClassLoader;
    }

    /**
     * 获取Sermant
     *
     * @param artifact 标识Sermant
     * @return SermantClassLoader
     */
    public static SermantClassLoader getSermant(String artifact) {
        return SERMANT_MANAGE_MAP.get(artifact);
    }

    /**
     * 移除Sermant
     *
     * @param artifact 需要移除的Sermant的命名空间
     * @throws RuntimeException RuntimeException
     */
    public static void removeSermant(String artifact) {
        SermantClassLoader sermantClassLoader = SERMANT_MANAGE_MAP.get(artifact);
        try {
            sermantClassLoader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SERMANT_MANAGE_MAP.remove(artifact);
    }

    /**
     * 检查Sermant产品状态
     *
     * @param artifact 产品名
     * @return boolean
     */
    public static boolean checkSermantStatus(String artifact) {
        Boolean status = SERMANT_STATUS.get(artifact);
        if (status == null) {
            return false;
        }
        return status;
    }

    /**
     * 更新Sermant产品状态
     *
     * @param artifact 产品名
     * @param status 状态
     */
    public static void updateSermantStatus(String artifact, boolean status) {
        SERMANT_STATUS.put(artifact, status);
    }

    /**
     * 当前产品是否安装过Sermant
     *
     * @param artifact 标识基于Sermant的产品
     * @return boolean
     */
    private static boolean hasSermant(String artifact) {
        return SERMANT_MANAGE_MAP.containsKey(artifact);
    }
}

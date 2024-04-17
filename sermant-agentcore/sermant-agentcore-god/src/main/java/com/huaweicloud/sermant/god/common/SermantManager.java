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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manage the Sermant installed in the host instance
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
     * Create Sermant
     *
     * @param artifact artifact that identities Sermant
     * @param urls Sermant resource path
     * @return SermantClassLoader
     */
    public static SermantClassLoader createSermant(String artifact, URL[] urls) {
        if (hasSermant(artifact)) {
            return SERMANT_MANAGE_MAP.get(artifact);
        }
        SermantClassLoader sermantClassLoader =
                AccessController.doPrivileged(new PrivilegedAction<SermantClassLoader>() {
                    @Override
                    public SermantClassLoader run() {
                        return new SermantClassLoader(urls);
                    }
                });
        SERMANT_MANAGE_MAP.put(artifact, sermantClassLoader);
        return sermantClassLoader;
    }

    /**
     * Get Sermant
     *
     * @param artifact artifact that identities Sermant
     * @return SermantClassLoader
     */
    public static SermantClassLoader getSermant(String artifact) {
        return SERMANT_MANAGE_MAP.get(artifact);
    }

    /**
     * Remove Sermant
     *
     * @param artifact artifact of the Sermant that needs to be removed
     * @throws RemoveSermantException RemoveSermantException
     */
    public static void removeSermant(String artifact) {
        SermantClassLoader sermantClassLoader = SERMANT_MANAGE_MAP.get(artifact);
        try {
            sermantClassLoader.close();
        } catch (IOException e) {
            throw new RemoveSermantException(e);
        }
        SERMANT_MANAGE_MAP.remove(artifact);
    }

    /**
     * Check Sermant product status
     *
     * @param artifact artifact that identities Sermant
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
     * Update Sermant product status
     *
     * @param artifact artifact that identities Sermant
     * @param status status
     */
    public static void updateSermantStatus(String artifact, boolean status) {
        SERMANT_STATUS.put(artifact, status);
    }

    /**
     * Whether the current instance has installed Sermant
     *
     * @param artifact artifact that identities Sermant
     * @return boolean
     */
    private static boolean hasSermant(String artifact) {
        return SERMANT_MANAGE_MAP.containsKey(artifact);
    }
}

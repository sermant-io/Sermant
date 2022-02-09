/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.service.dynamicconfig.zookeeper;

import java.util.Locale;

/**
 * zookeeper客户端初始化异常
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-12-15
 */
public class ZooKeeperInitException extends RuntimeException {
    /**
     * zookeeper断开连接
     */
    public ZooKeeperInitException() {
        super("ZooKeeper client disconnected from server. ");
    }

    /**
     * zookeeper连接失败
     *
     * @param connectString 连接字符串
     */
    public ZooKeeperInitException(String connectString) {
        super(String.format(Locale.ROOT, "Connect to %s failed. ", connectString));
    }
}

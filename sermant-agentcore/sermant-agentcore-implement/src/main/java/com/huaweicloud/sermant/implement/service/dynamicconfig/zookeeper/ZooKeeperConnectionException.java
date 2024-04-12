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

package com.huaweicloud.sermant.implement.service.dynamicconfig.zookeeper;

/**
 * ZooKeeper connection exception
 *
 * @author zhp
 * @since 2023-09-12
 */
public class ZooKeeperConnectionException extends RuntimeException {
    private static final long serialVersionUID = 5111014183835362572L;

    /**
     * ZooKeeper connection exception
     *
     * @param exceptionMessage connection message
     */
    public ZooKeeperConnectionException(String exceptionMessage) {
        super(exceptionMessage);
    }
}

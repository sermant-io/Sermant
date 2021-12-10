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

package com.huawei.javamesh.core.lubanops.bootstrap.collector.api;

import java.util.HashMap;

/**
 * 监控数据的一行，以map形式标示 <br>
 *
 * @author
 * @since 2020年3月13日
 */
public class MonitorDataRow extends HashMap<String, Object> {

    /**
     *
     */
    private static final long serialVersionUID = -2152467628560139889L;

    public MonitorDataRow(int size) {
        super(size);
    }

    public MonitorDataRow() {
        super();
    }

    /**
     * add a data row with fluent style
     *
     * @param rowKey
     * @param rowValue
     * @return
     */
    public MonitorDataRow add(String rowKey, Object rowValue) {
        super.put(rowKey, rowValue);
        return this;
    }

}

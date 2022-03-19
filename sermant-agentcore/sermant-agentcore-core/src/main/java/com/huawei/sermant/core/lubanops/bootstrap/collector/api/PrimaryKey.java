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

package com.huawei.sermant.core.lubanops.bootstrap.collector.api;

import com.huawei.sermant.core.lubanops.bootstrap.exception.ApmRuntimeException;

import java.util.Arrays;

/**
 * 代表一行监控数据的主键，这个类重载了hashCode和equals方法，可以当作hash相关的map的key用
 *
 * @author frank.yef
 */
public class PrimaryKey {
    private String[] keys;

    public PrimaryKey(String... pks) {

        if (pks == null || pks.length < 1) {
            throw new ApmRuntimeException("must have at least one");
        }
        keys = pks;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (String s : keys) {
            sb.append(s).append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public String get(int index) {
        return keys[index];
    }

    public int getKeyLength() {
        return keys.length;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.keys);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof PrimaryKey)) {
            return false;
        }
        PrimaryKey pk = (PrimaryKey) o;
        return Arrays.equals(this.keys, pk.keys);
    }

}

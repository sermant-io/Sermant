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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.huawei.sermant.core.lubanops.bootstrap.exception.ApmRuntimeException;

/**
 * 对于只有多个主键的采集器的基类
 * @param <T>
 * @author frank.yef
 */
public abstract class MultiPrimaryKeyAggregator<T extends StatsBase>
        extends AbstractPrimaryKeyValueAggregator<PrimaryKey, T> {

    /**
     * 默认主键
     */
    protected PrimaryKey defaultKey() {
        int len = primaryKeyLength();
        String[] ss = new String[len];
        for (int i = 0; i < ss.length; i++) {
            ss[i] = DEFAULT_KEY;
        }
        return new PrimaryKey(ss);
    }

    /**
     * 主键的长度,由子类定义
     */
    protected abstract int primaryKeyLength();

    /**
     * 根据主键生成值对象
     * @param pks
     * @return
     */
    protected T getValue(String... pks) {
        int c = primaryKeyLength();
        if (pks.length != c) {
            throw new ApmRuntimeException(
                    "primary key field count must equal with what you defined:" + c + ",actual:" + pks.length);
        }
        PrimaryKey pk = new PrimaryKey(pks);
        return super.getValue(pk);
    }

    /**
     * 根据主键删除对应的数据
     * @param pks
     */
    protected void removeValue(String... pks) {
        int c = primaryKeyLength();
        if (pks.length != c) {
            throw new ApmRuntimeException(
                    "primary key field count must equal with what you defined:" + c + ",actual:" + pks.length);
        }
        PrimaryKey pk = new PrimaryKey(pks);
        super.removeValue(pk);
    }

    @Override
    public PrimaryKey getPrimaryKey(Map<String, String> primaryKeyMap) {
        List<String> keys = primaryKey();
        List<String> values = new ArrayList<String>();
        for (String key : keys) {
            String value = primaryKeyMap.get(key);
            if (value == null) {
                throw new ApmRuntimeException("Key " + key + " is null");
            }
            values.add(value);
        }
        return new PrimaryKey(values.toArray(new String[0]));
    }

    @Override
    protected void setPrimaryKey(MonitorDataRow row, PrimaryKey primaryKey) {
        List<String> keys = primaryKey();
        for (int i = 0; i < keys.size(); i++) {
            row.put(keys.get(i), primaryKey.get(i));
        }
    }

    /**
     * 主键
     */
    protected abstract List<String> primaryKey();
}

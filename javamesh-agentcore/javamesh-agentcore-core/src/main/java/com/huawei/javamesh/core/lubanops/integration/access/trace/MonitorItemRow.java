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

package com.huawei.javamesh.core.lubanops.integration.access.trace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.huawei.javamesh.core.lubanops.integration.utils.TimeUtil;

/**
 * 监控数据的一行
 *
 * @author
 */

public class MonitorItemRow {
    /*
     * 采集的时间,单位是毫秒
     */
    private long time;

    /*
     * 模型的属性部分，value的Object类型可能的类有String，Number
     */
    private Map<String, Object> entries = new HashMap<String, Object>();

    public MonitorItemRow() {

    }

    public MonitorItemRow(long t) {
        time = t;
    }

    public MonitorItemRow(long t, Map<String, Object> entries) {
        this.time = t;
        this.entries = entries;
    }

    /*
     * 生成当前对象的拷贝，并且去掉一些字段
     */
    public MonitorItemRow makeCopyOfAndRemoveFields(Set<String> trimedFields) {
        MonitorItemRow newItem = new MonitorItemRow();
        newItem.setTime(this.time);
        Map<String, Object> newEntries = new HashMap<String, Object>();
        newEntries.putAll(entries);
        if (trimedFields != null && !trimedFields.isEmpty()) {
            for (String s : trimedFields) {
                newEntries.remove(s);
            }
        }

        newItem.setEntries(newEntries);

        return newItem;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(time:").append(TimeUtil.getDefaultInstance().formatWithDefault(time));
        sb.append(",data entries:[");
        if (entries != null) {
            Set<Map.Entry<String, Object>> enset = entries.entrySet();
            Iterator<Map.Entry<String, Object>> it = enset.iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                sb.append(entry.getKey()).append("=");
                Object v = entry.getValue();
                if (v == null) {
                    sb.append(",");
                } else {
                    sb.append(v.toString()).append(",");
                }

            }
        }
        sb.append("]");
        sb.append(")");
        return sb.toString();
    }

    /**
     * 获取某个key的值
     *
     * @param key
     * @return
     */
    public Object getValue(String key) {
        return this.entries.get(key);
    }

    public void put(String key, Object value) {
        entries.put(key, value);
    }

    public void putAll(Map<String, Object> all) {
        entries.putAll(all);
    }

    public Map<String, Object> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, Object> entries) {
        this.entries = entries;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long collectDate) {
        this.time = collectDate;
    }

}

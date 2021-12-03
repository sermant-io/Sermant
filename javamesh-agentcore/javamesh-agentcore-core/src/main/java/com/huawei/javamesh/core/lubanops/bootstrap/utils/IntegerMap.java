/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.lubanops.bootstrap.utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 用byte下标当做key的map，这种map的特点是定位value速度比较快，内部是一个数组
 * @author hzyefeng
 */
public class IntegerMap {
    private String[] resources;

    private ConcurrentHashMap<String, Integer> concurrentHashMap = new ConcurrentHashMap<String, Integer>();

    private volatile boolean isfull = false;

    private volatile int size = 0;

    private volatile int max;

    public IntegerMap(int max) {
        if (max > 1000) {
            throw new RuntimeException("exeed the max value:" + 1000);
        }
        resources = new String[max];
        this.max = max;

    }

    public int registerResource(String r) {
        Integer b = concurrentHashMap.get(r);
        if (b != null) {
            return b;
        }

        if (isfull) {
            return -1;
        }
        return syncRegister(r);

    }

    private synchronized int syncRegister(String r) {
        Integer b = concurrentHashMap.get(r);
        if (b != null) {
            return b;
        }
        resources[size] = r;
        concurrentHashMap.put(r, size);
        int c = size;
        size++;
        if (size >= max) {
            isfull = true;
        }
        return c;
    }

    public int size() {
        return size;
    }

    public final String getResource(int b) {
        return resources[b];
    }

}

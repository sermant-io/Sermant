/**
 *
 */

package com.huawei.apm.core.lubanops.bootstrap.utils;

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

/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */
package com.lubanops.stresstest.redis.lettuce.pool;

import com.lubanops.stresstest.core.Tester;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * GenericObjectPool proxy
 * @param <T> type
 * @author yiwei
 * @since 2021/11/4
 */
public class GenericObjectPoolProxy<T> extends GenericObjectPool<T> {
    private GenericObjectPool<T> originalPool;

    public GenericObjectPoolProxy(PooledObjectFactory<T> factory, GenericObjectPoolConfig<T> config, GenericObjectPool<T> originalPool) {
        super(factory, config);
        this.originalPool = originalPool;
    }

    @Override
    public T borrowObject() throws Exception {
        if (Tester.isTest()) {
            return super.borrowObject();
        }
        return originalPool.borrowObject();
    }

    @Override
    public void returnObject(T obj) {
        if (Tester.isTest()) {
            super.returnObject(obj);
        } else {
            originalPool.returnObject(obj);
        }
    }
}

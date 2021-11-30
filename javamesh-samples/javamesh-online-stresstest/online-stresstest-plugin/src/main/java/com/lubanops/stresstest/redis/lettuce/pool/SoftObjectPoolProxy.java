/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */
package com.lubanops.stresstest.redis.lettuce.pool;

import com.lubanops.stresstest.core.Tester;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.SoftReferenceObjectPool;

/**
 * SoftReferenceObjectPool proxy
 *
 * @param <T> type
 * @author yiwei
 * @since 2021/11/4
 */
public class SoftObjectPoolProxy<T> extends SoftReferenceObjectPool<T> {
    private SoftReferenceObjectPool<T> originalPool;

    public SoftObjectPoolProxy(PooledObjectFactory<T> factory, SoftReferenceObjectPool<T> originalPool) {
        super(factory);
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
    public void returnObject(T obj) throws Exception {
        if (Tester.isTest()) {
            super.returnObject(obj);
        } else {
            originalPool.returnObject(obj);
        }
    }
}

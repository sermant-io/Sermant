/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.stresstest.redis.lettuce.pool;

import com.huawei.sermant.stresstest.core.Tester;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.SoftReferenceObjectPool;

/**
 * SoftReferenceObjectPool proxy
 *
 * @param <T> type
 * @author yiwei
 * @since 2021-11-04
 */
public class SoftObjectPoolProxy<T> extends SoftReferenceObjectPool<T> {
    private SoftReferenceObjectPool<T> originalPool;

    /**
     * 构造方法
     *
     * @param factory factory
     * @param originalPool originalPool
     */
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

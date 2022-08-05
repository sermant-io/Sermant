/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.loadbalancer.factory;

import java.util.concurrent.ThreadFactory;

/**
 * 线程工厂
 *
 * @author zhouss
 * @since 2022-08-04
 */
public class LoadbalancerThreadFactory implements ThreadFactory {
    private final String threadName;

    /**
     * 线程工厂构造器
     *
     * @param threadName 线程名字
     */
    public LoadbalancerThreadFactory(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, threadName);
    }
}

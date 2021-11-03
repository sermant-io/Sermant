/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.service;

import com.huawei.apm.core.exception.ServiceInitException;
import com.huawei.apm.core.util.SpiLoadUtil;

/**
 * Agent核心服务接口
 *
 * <p>实现该接口的服务实例会在agent启动时被创建，在所有<Code>CoreService<Code/>实例
 * 被创建完成之后再依次调用的它们的{@link #start()}方法，并为所有服务实例创建shutdown
 * hook，用于在虚拟机结束时调用服务是{@link #stop()}方法</p>
 *
 * <p>所有<Code>CoreService<Code/>实例将由{@link CoreServiceManager}管理，可以
 * 通过{@link CoreServiceManager#getService(Class)}获取相应的服务实例，其中参数
 * <code>serviceClass</code>为除<Code>CoreService<Code/>外的其他接口的类型，该
 * 接口将作为<Code>CoreService<Code/>实现类的功能接口。每个功能接口只有一个实现类被
 * 加载，该实现为默认实现。如果需要替换默认实现，需要在用于替换的实现类上添加{@link SpiLoadUtil.SpiWeight}
 * 注解，每一个功能接口有且仅有一个默认实现和添加了{@link SpiLoadUtil.SpiWeight}注解实现存在于
 * ClassPath，否则将会抛出{@link ServiceInitException}。<p/>
 */
public interface CoreService {

    /**
     * 服务启动方法
     */
    void start();

    /**
     * 服务关闭方法
     */
    void stop();
}

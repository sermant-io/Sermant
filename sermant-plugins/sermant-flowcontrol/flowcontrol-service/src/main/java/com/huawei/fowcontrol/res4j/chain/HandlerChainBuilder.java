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

package com.huawei.fowcontrol.res4j.chain;

import com.huawei.flowcontrol.common.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * 链构建器, 将所有spi加载的processor按照优先级串起来
 *
 * @author zhouss
 * @since 2022-07-05
 */
public enum HandlerChainBuilder {
    /**
     * 单例
     */
    INSTANCE;

    private static final int HANDLER_SIZE = 4;

    private static final List<AbstractChainHandler> HANDLERS = new ArrayList<>(HANDLER_SIZE);

    static {
        for (AbstractChainHandler handler : ServiceLoader.load(AbstractChainHandler.class, HandlerChainBuilder.class
                .getClassLoader())) {
            HANDLERS.add(handler);
        }
    }

    /**
     * 构建链
     *
     * @return ProcessorChain 执行链
     */
    public HandlerChain build() {
        final HandlerChain processorChain = new HandlerChain();
        Collections.sort(HANDLERS);
        HANDLERS.forEach(processorChain::addLastHandler);
        return processorChain;
    }

    /**
     * 获取处理链
     *
     * @param name 名称
     * @return 处理链
     */
    public static Optional<AbstractChainHandler> getHandler(String name) {
        for (AbstractChainHandler abstractChainHandler : HANDLERS) {
            if (StringUtils.equal(abstractChainHandler.getClass().getName(), name)) {
                return Optional.of(abstractChainHandler);
            }
        }
        return Optional.empty();
    }
}

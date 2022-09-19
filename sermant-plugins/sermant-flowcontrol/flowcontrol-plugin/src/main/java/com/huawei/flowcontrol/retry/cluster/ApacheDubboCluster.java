/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.retry.cluster;

import com.huawei.flowcontrol.common.config.FlowControlConfig;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Cluster;
import org.apache.dubbo.rpc.cluster.Directory;

import java.util.Optional;

/**
 * apache dubbo调用实现
 *
 * @author zhouss
 * @since 2022-03-04
 */
public class ApacheDubboCluster implements Cluster {
    @Override
    public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
        return this.join(directory, false);
    }

    /**
     * 该方法在dubbo3.x.x版本之后需实现
     *
     * @param directory 服务信息
     * @param buildFilterChain 是否构建过滤链
     * @param <T> 响应类型
     * @return Invoker
     * @throws RpcException 调用异常抛出
     */
    public <T> Invoker<T> join(Directory<T> directory, boolean buildFilterChain) throws RpcException {
        final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
        if (!pluginConfig.isUseOriginInvoker()) {
            return new ApacheDubboClusterInvoker<>(directory);
        }
        Invoker<T> delegate = null;
        Object curCluster = ClusterInvokerCreator.INSTANCE.buildInvoker();
        if (curCluster instanceof Cluster) {
            if (isDubbo3x(curCluster)) {
                final Optional<Object> join = ReflectUtils
                        .invokeMethod(curCluster, "join", new Class[]{Directory.class, boolean.class},
                                new Object[]{directory, buildFilterChain});
                if (join.isPresent()) {
                    delegate = (Invoker<T>) join.get();
                }
            } else {
                delegate = ((Cluster)curCluster).join(directory);
            }
        }
        if (delegate != null) {
            return new ApacheDubboClusterInvoker<>(directory, delegate);
        }
        return new ApacheDubboClusterInvoker<>(directory);
    }

    private boolean isDubbo3x(Object curCluster) {
        return !ReflectUtils.findMethod(curCluster.getClass(), "join", new Class[] {Directory.class})
                .isPresent();
    }
}

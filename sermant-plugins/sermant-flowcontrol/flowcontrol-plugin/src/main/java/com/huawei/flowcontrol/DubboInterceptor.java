/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/skywalking/apm/plugin/asf/dubbo/DubboInterceptor.java
 * and org/apache/skywalking/apm/plugin/dubbo/DubboInterceptor.java
 * from the Apache Skywalking project.
 */

package com.huawei.flowcontrol;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.huawei.flowcontrol.core.config.FlowControlConfig;
import com.huawei.flowcontrol.entry.EntryFacade;
import com.huawei.flowcontrol.exception.FlowControlException;
import com.huawei.flowcontrol.util.SentinelRuleUtil;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

import java.util.Locale;

public abstract class DubboInterceptor implements InstanceMethodInterceptor {

    private FlowControlConfig flowControlConfig;

    private FlowControlConfig getFlowControlConfig() {
        if (flowControlConfig == null) {
            flowControlConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
        }
        return flowControlConfig;
    }

    protected String getResourceName(String interfaceName, String methodName) {
        return interfaceName + ":" + methodName;
    }

    protected void handleBlockException(BlockException ex, String resourceName, String type,
                                        EntryFacade.DubboType dubboType) {
        try {
            final String msg = String.format(Locale.ENGLISH,
                "[%s] has been blocked! [appName=%s, resourceName=%s]",
                type, ex.getRuleLimitApp(), resourceName);
            RecordLog.info(msg);
            if (getFlowControlConfig().isThrowBizException()) {
                // 开启业务异常抛出，将会取代基于返回结果形式将异常返回给上游, 并且会触发dubbo重试, 默认关闭该功能
                String res = SentinelRuleUtil.getResult(ex.getRule());
                throw new FlowControlException(res);
            }
        } finally {
            if (EntryFacade.DubboType.APACHE == dubboType) {
                EntryFacade.INSTANCE.exit(dubboType);
            } else {
                EntryFacade.INSTANCE.exit(dubboType);
            }
        }
    }
}

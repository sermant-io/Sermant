/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.register.interceptors;

import com.huawei.register.context.RegisterContext;
import com.huawei.register.entity.FixedResult;
import com.huawei.register.services.RegisterCenterService;
import com.huawei.register.support.RegisterSwitchSupport;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.service.ServiceManager;

import org.springframework.cloud.client.serviceregistry.Registration;

/**
 * 拦截获取服务列表
 *
 * @author zhouss
 * @since 2021-12-13
 */
public class RegistrationInterceptor extends RegisterSwitchSupport {
    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (!(context.getArguments()[0] instanceof Registration)) {
            return context;
        }
        fillClientInfo((Registration) context.getArguments()[0]);
        final RegisterCenterService service = ServiceManager.getService(RegisterCenterService.class);
        final FixedResult fixedResult = new FixedResult();
        service.register(fixedResult);
        if (fixedResult.isSkip()) {
            context.skip(fixedResult.getResult());
        }
        return context;
    }

    private void fillClientInfo(Registration registration) {
        RegisterContext.INSTANCE.getClientInfo().setHost(registration.getHost());
        RegisterContext.INSTANCE.getClientInfo().setMeta(registration.getMetadata());
        RegisterContext.INSTANCE.getClientInfo().setPort(registration.getPort());
        RegisterContext.INSTANCE.getClientInfo().setServiceId(registration.getServiceId());
    }
}

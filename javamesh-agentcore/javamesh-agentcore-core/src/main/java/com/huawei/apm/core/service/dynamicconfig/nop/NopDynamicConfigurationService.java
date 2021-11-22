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
package com.huawei.apm.core.service.dynamicconfig.nop;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.service.dynamicconfig.Config;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigurationService;

import java.util.logging.Logger;

/**
 *   This class is for testing purpose only.
 */
@Deprecated
public class NopDynamicConfigurationService implements DynamicConfigurationService {


    private static final Logger logger = LogFactory.getLogger();

    static private NopDynamicConfigurationService serviceInst;

    private NopDynamicConfigurationService() {
        // no-op
    }

    public static synchronized NopDynamicConfigurationService getInstance()
    {
        if ( serviceInst == null )
        {
            serviceInst = new NopDynamicConfigurationService();
        }
        return serviceInst;
    }


    @Override
    public boolean addListener(String key, String group, ConfigurationListener listener) {
        return true;
    }

    @Override
    public boolean removeListener(String key, String group, ConfigurationListener listener) {
        return true;
    }

    @Override
    public String getConfig(String key, String group) throws IllegalStateException {
        // no-op
        return "";
    }

    /**
     * @since 2.7.5
     */
    @Override
    public boolean publishConfig(String key, String group, String content) {
        return true;
    }

    @Override
    public String getDefaultGroup() {
        return Config.getDefaultGroup();
    }

    @Override
    public long getDefaultTimeout() {
        return Config.getTimeout_value();
    }

    @Override
    public void close() throws Exception {
        // no-op
    }

}

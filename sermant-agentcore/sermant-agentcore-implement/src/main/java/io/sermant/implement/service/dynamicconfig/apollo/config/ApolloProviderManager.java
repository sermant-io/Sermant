/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.implement.service.dynamicconfig.apollo.config;

import com.ctrip.framework.foundation.internals.DefaultProviderManager;
import com.ctrip.framework.foundation.internals.provider.DefaultNetworkProvider;
import com.ctrip.framework.foundation.spi.provider.Provider;

/**
 * Override the initialize method to load customized providers
 *
 * @author Chen Zhenyang
 * @since 2025-08-14
 */
public class ApolloProviderManager extends DefaultProviderManager {
    /**
     * initialize customized providers
     */
    @Override
    public void initialize() {
        Provider applicationProvider = new ApolloApplicationProvider();
        applicationProvider.initialize();
        this.register(applicationProvider);
        Provider networkProvider = new DefaultNetworkProvider();
        networkProvider.initialize();
        this.register(networkProvider);
        Provider serverProvider = new ApolloServerProvider();
        serverProvider.initialize();
        this.register(serverProvider);
    }
}

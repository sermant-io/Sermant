/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.implement.service.xds;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.XdsServiceDiscovery;
import io.sermant.implement.service.xds.client.XdsClient;
import io.sermant.implement.service.xds.discovery.XdsServiceDiscoveryImpl;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * XdsCoreService impl
 *
 * @author daizhenyu
 * @since 2024-05-21
 **/
public class XdsCoreServiceImpl implements XdsCoreService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private XdsServiceDiscovery xdsServiceDiscovery;

    private XdsClient client;

    @Override
    public void start() {
        client = new XdsClient();
        xdsServiceDiscovery = new XdsServiceDiscoveryImpl(client);
    }

    @Override
    public void stop() {
        try {
            client.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Occur error when close the XdsClient.", e);
        }
    }

    @Override
    public XdsServiceDiscovery getXdsServiceDiscovery() {
        return xdsServiceDiscovery;
    }
}

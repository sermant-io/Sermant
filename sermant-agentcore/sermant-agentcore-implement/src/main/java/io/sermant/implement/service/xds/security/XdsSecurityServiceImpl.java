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

package io.sermant.implement.service.xds.security;

import io.sermant.core.service.xds.XdsSecurityService;
import io.sermant.core.service.xds.entity.IstiodCertificate;
import io.sermant.core.service.xds.entity.XdsAuthorizationRule;
import io.sermant.implement.service.xds.cache.XdsDataCache;

/**
 * Xds Security Service
 *
 * @author lilai
 * @since 2025-06-04
 */
public class XdsSecurityServiceImpl implements XdsSecurityService {
    @Override
    public boolean isSslEnabled() {
        return XdsDataCache.isSslEnabled();
    }

    @Override
    public IstiodCertificate getIstiodCertificate() {
        return XdsDataCache.getIstiodCertificate();
    }

    @Override
    public XdsAuthorizationRule getXdsAuthorizationRule() {
        return XdsDataCache.getXdsAuthorizationRule();
    }
}

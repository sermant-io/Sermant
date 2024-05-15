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

package io.sermant.core.service.xds.config;

import io.sermant.core.config.common.BaseConfig;
import io.sermant.core.config.common.ConfigFieldKey;
import io.sermant.core.config.common.ConfigTypeKey;

/**
 * XdsConfig
 *
 * @author daizhenyu
 * @since 2024-05-08
 **/
@ConfigTypeKey("xds.config")
public class XdsConfig implements BaseConfig {
    @ConfigFieldKey("control.plane.address")
    private String controlPlaneAddress;

    @ConfigFieldKey("security.enable")
    private boolean securityEnable = false;

    @ConfigFieldKey("certificate.path")
    private String certificatePath;

    @ConfigFieldKey("private.key.path")
    private String privateKeyPath;

    public String getControlPlaneAddress() {
        return controlPlaneAddress;
    }

    public void setControlPlaneAddress(String controlPlaneAddress) {
        this.controlPlaneAddress = controlPlaneAddress;
    }

    public boolean isSecurityEnable() {
        return securityEnable;
    }

    public void setSecurityEnable(boolean securityEnable) {
        this.securityEnable = securityEnable;
    }

    public String getCertificatePath() {
        return certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }
}

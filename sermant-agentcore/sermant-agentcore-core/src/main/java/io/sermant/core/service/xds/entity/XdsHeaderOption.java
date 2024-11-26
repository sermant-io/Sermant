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

package io.sermant.core.service.xds.entity;

/**
 * Header name/ value pair plus option to control append behavior
 *
 * @author zhp
 * @since 2024-11-21
 */
public class XdsHeaderOption {
    /**
     * Header name/ value pair that this option applies to
     */
    private XdsHeader header;

    /**
     * Should the value be appended
     */
    private boolean enabledAppend;

    public XdsHeader getHeader() {
        return header;
    }

    public void setHeader(XdsHeader header) {
        this.header = header;
    }

    public boolean isEnabledAppend() {
        return enabledAppend;
    }

    public void setEnabledAppend(boolean enabledAppend) {
        this.enabledAppend = enabledAppend;
    }
}

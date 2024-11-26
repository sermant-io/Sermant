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
 * Xds fault information
 *
 * @author zhp
 * @since 2024-11-18
 */
public class XdsHttpFault {
    /**
     * abort information, If specified, the filter will abort requests based on the values in the object
     */
    private XdsAbort abort;

    /**
     * delay information, If specified, the filter will inject delays based on the values in the object
     */
    private XdsDelay delay;

    public XdsAbort getAbort() {
        return abort;
    }

    public void setAbort(XdsAbort abort) {
        this.abort = abort;
    }

    public XdsDelay getDelay() {
        return delay;
    }

    public void setDelay(XdsDelay delay) {
        this.delay = delay;
    }
}

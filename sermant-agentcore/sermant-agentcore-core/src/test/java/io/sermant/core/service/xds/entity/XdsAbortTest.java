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

import org.junit.Assert;
import org.junit.Test;

/**
 * XdsAbortTest
 *
 * @author zhp
 * @since 2024-11-21
 **/
public class XdsAbortTest {
    @Test
    public void testXdsAbort() {
        XdsAbort abort = new XdsAbort();
        abort.setPercentage(100);
        abort.setHttpStatus(200);
        Assert.assertEquals(200, abort.getHttpStatus());
        Assert.assertEquals(100, abort.getPercentage(), 0);
    }
}

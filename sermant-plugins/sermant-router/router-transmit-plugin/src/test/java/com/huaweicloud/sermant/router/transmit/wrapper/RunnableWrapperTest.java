/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.transmit.wrapper;

import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.request.RequestHeader;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.transmit.BaseTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * 测试RunnableWrapper
 *
 * @author provenceee
 * @since 2023-05-27
 */
public class RunnableWrapperTest extends BaseTest {
    @Test
    public void testCanTransmit() {
        RunnableWrapper<?> wrapper = new RunnableWrapper<>(() -> {
            Assert.assertNotNull(ThreadLocalUtils.getRequestData());
            Assert.assertNotNull(ThreadLocalUtils.getRequestHeader());
        }, new RequestHeader(null), new RequestData(null, null, null), false);

        Assert.assertNull(ThreadLocalUtils.getRequestData());
        Assert.assertNull(ThreadLocalUtils.getRequestHeader());

        wrapper.run();

        Assert.assertNull(ThreadLocalUtils.getRequestData());
        Assert.assertNull(ThreadLocalUtils.getRequestHeader());
    }

    @Test
    public void testCannotTransmit() {
        // 初始条件
        ThreadLocalUtils.setRequestHeader(new RequestHeader(null));
        ThreadLocalUtils.setRequestData(new RequestData(null, null, null));

        Assert.assertNotNull(ThreadLocalUtils.getRequestData());
        Assert.assertNotNull(ThreadLocalUtils.getRequestHeader());

        RunnableWrapper<Object> wrapper = new RunnableWrapper<>(() -> {
            Assert.assertNull(ThreadLocalUtils.getRequestData());
            Assert.assertNull(ThreadLocalUtils.getRequestHeader());
        }, new RequestHeader(null), new RequestData(null, null, null), true);

        Assert.assertNull(ReflectUtils.getFieldValue(wrapper, "requestData").orElse(null));
        Assert.assertNull(ReflectUtils.getFieldValue(wrapper, "requestHeader").orElse(null));

        wrapper.run();

        Assert.assertNull(ThreadLocalUtils.getRequestData());
        Assert.assertNull(ThreadLocalUtils.getRequestHeader());
    }
}
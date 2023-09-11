/*
 *   Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.tag.transmission.utils;

import org.apache.dubbo.rpc.RpcInvocation;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

/**
 * DubboUtils工具类测试
 *
 * @author daizhenyu
 * @since 2023-08-09
 **/
public class DubboUtilsTest {
    private static final String ATTACHMENTS_FIELD = "attachments";

    /**
     * 测试DubboUtils工具类的getAttachmentsByInvocation方法
     */
    @Test
    public void getAttachmentsByInvocationTest() {
        RpcInvocation rpcInvocation;
        Optional<Object> expectOptional;

        // 输入对象为null
        rpcInvocation = null;
        expectOptional = Optional.empty();
        Assert.assertEquals(DubboUtils.getAttachmentsByInvocation(rpcInvocation), expectOptional);

        // 输入对象不为空, attachments参数为null
        rpcInvocation = new RpcInvocation();
        expectOptional = Optional.empty();
        Assert.assertEquals(DubboUtils.getAttachmentsByInvocation(rpcInvocation), expectOptional);

        // 输入对象不为空，attachments参数不为null
        rpcInvocation = new RpcInvocation();
        rpcInvocation.setAttachment("key", "value");
        expectOptional = Optional.of(rpcInvocation.getObjectAttachments());
        Assert.assertEquals(DubboUtils.getAttachmentsByInvocation(rpcInvocation), expectOptional);
    }
}

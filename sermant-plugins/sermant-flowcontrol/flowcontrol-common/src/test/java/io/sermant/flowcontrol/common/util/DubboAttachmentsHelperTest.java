/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.flowcontrol.common.util;

import org.apache.dubbo.rpc.RpcContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * attachments resolution test
 *
 * @author zhouss
 * @since 2022-09-19
 */
public class DubboAttachmentsHelperTest {
    @Test
    public void testNull() {
        final Map<String, String> map = DubboAttachmentsHelper.resolveAttachments(null, false);
        Assert.assertEquals(map, Collections.emptyMap());
        final Map<String, String> map2 = DubboAttachmentsHelper.resolveAttachments(null, true);
        Assert.assertEquals(map2, Collections.emptyMap());
    }

    @Test
    public void testAlibabaInvocation() {
        final TestStringInvocation testStringInvocation = new TestStringInvocation(null);
        String key = "a";
        String value = "c";
        com.alibaba.dubbo.rpc.RpcContext.getContext().getAttachments().put(key, value);
        final Map<String, String> map = DubboAttachmentsHelper.resolveAttachments(testStringInvocation, false);
        Assert.assertEquals(map.get(key), value);
        com.alibaba.dubbo.rpc.RpcContext.getContext().getAttachments().clear();
    }

    @Test
    public void testApacheInvocation() {
        final TestObjectInvocation testObjectInvocation = new TestObjectInvocation(null);
        String key = "a";
        String value = "apache";
        RpcContext.getContext().getAttachments().put(key, value);
        final Map<String, String> map = DubboAttachmentsHelper.resolveAttachments(testObjectInvocation, true);
        Assert.assertEquals(map.get(key), value);
        RpcContext.getContext().getAttachments().clear();
    }

    @Test
    public void testStringAttachments() {
        final TestStringInvocation testStringInvocation = new TestStringInvocation(buildAttachments());
        final Map<String, String> attachmentsByString = DubboAttachmentsHelper
                .resolveAttachments(testStringInvocation, false);
        Assert.assertEquals(attachmentsByString, testStringInvocation.attachments);
        final Map<String, String> attachmentsByString2 = DubboAttachmentsHelper
                .resolveAttachments(testStringInvocation, true);
        Assert.assertEquals(attachmentsByString2, testStringInvocation.attachments);
    }

    private Map<String, String> buildAttachments() {
        final Map<String, String> stringAttachments = new HashMap<>();
        stringAttachments.put("a", "b");
        stringAttachments.put("c", "d");
        return stringAttachments;
    }

    private Map<String, Object> buildObjectAttachments() {
        final Map<String, Object> objectAttachments = new HashMap<>();
        objectAttachments.put("a", new Object());
        objectAttachments.put("c", new TestObjectInvocation(null));
        return objectAttachments;
    }

    @Test
    public void testObjectAttachments() {
        final TestObjectInvocation testObjectInvocation = new TestObjectInvocation(buildObjectAttachments());
        final Map<String, String> attachmentsByObject = DubboAttachmentsHelper
                .resolveAttachments(testObjectInvocation, false);
        Assert.assertEquals(attachmentsByObject, testObjectInvocation.attachments);
        final Map<String, String> attachmentsByObject2 = DubboAttachmentsHelper
                .resolveAttachments(testObjectInvocation, true);
        Assert.assertEquals(attachmentsByObject2, testObjectInvocation.attachments);
    }

    static class TestStringInvocation {
        private final Map<String, String> attachments;

        TestStringInvocation(Map<String, String> attachments) {
            this.attachments = attachments;
        }
    }

    static class TestObjectInvocation {
        private final Map<String, Object> attachments;

        TestObjectInvocation(Map<String, Object> attachments) {
            this.attachments = attachments;
        }
    }
}

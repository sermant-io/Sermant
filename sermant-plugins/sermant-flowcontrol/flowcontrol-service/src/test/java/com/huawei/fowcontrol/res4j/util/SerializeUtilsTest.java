/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.fowcontrol.res4j.util;

import com.huawei.flowcontrol.common.core.rule.fault.FaultException;
import com.huawei.flowcontrol.common.core.rule.fault.FaultRule;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

/**
 * 序列化工具测试
 *
 * @author zhouss
 * @since 2022-08-30
 */
public class SerializeUtilsTest {
    /**
     * 测试序列化各类对象
     */
    @Test
    public void test() {
        final Object target = new Object();
        final Optional<String> serialize2String = SerializeUtils.serialize2String(target);
        Assert.assertTrue(serialize2String.isPresent());
        final Optional<String> serialize2String1 = SerializeUtils.serialize2String(null);
        Assert.assertTrue(serialize2String1.isPresent());
        Assert.assertEquals("", serialize2String1.get());
        final Optional<String> serialize2String2 =
                SerializeUtils.serialize2String(new FaultException(-1, "test", new FaultRule()));
        Assert.assertTrue(serialize2String2.isPresent());
    }
}

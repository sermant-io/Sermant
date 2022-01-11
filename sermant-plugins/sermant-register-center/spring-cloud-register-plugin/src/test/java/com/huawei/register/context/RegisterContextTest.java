/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.register.context;

import com.huawei.register.handler.SingleStateCloseHandler;
import org.junit.Assert;
import org.junit.Test;

/**
 * handler测试
 *
 * @author zhouss
 * @since 2022-01-05
 */
public class RegisterContextTest {

    @Test
    public void testRegisterHandler() {
        RegisterContext.INSTANCE.registerCloseHandler(new SingleStateCloseHandler() {
            @Override
            protected void close() {

            }
        });
        // 自身注册， 加上主动注册
        Assert.assertEquals(2, RegisterContext.INSTANCE.getCloseHandlers().size());
    }

    @Test
    public void testAvailable() {
        RegisterContext.INSTANCE.setAvailable(true);
        Assert.assertTrue(RegisterContext.INSTANCE.isAvailable());
        RegisterContext.INSTANCE.setAvailable(false);
        Assert.assertFalse(RegisterContext.INSTANCE.isAvailable());
    }
}

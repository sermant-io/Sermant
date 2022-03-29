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

package com.huawei.sermant.stresstest.redis.jedis.command;

import com.huawei.sermant.stresstest.core.Tester;
import com.huawei.sermant.stresstest.redis.RedisUtils;

/**
 * 处理redis key
 *
 * @author yiwei
 * @since 2021-11-01
 */
public interface Handler {
    /**
     * handle
     *
     * @param bytes bytes
     * @return byte二维数组
     */
    byte[][] handle(byte[][] bytes);

    /**
     * addShadowPrefix
     *
     * @param start start
     * @param end end
     * @param gap gap
     * @param args args
     * @return byte二维数组
     */
    default byte[][] addShadowPrefix(int start, int end, int gap, byte[][] args) {
        if (!Tester.isTest()) {
            return args;
        }
        int length = args.length;
        byte[][] results = new byte[args.length][];
        if (length > 0) {
            for (int i = start; i < end; i = i + gap) {
                results[i] = RedisUtils.modifyBytes(args[i]);
                if (gap - 1 > 0) {
                    System.arraycopy(args, i + 1, results, i + 1, gap - 1);
                }
            }
            for (int i = end; i < length; i = i + 1) {
                System.arraycopy(args, i, results, i, length - end);
            }
        }
        return results;
    }
}

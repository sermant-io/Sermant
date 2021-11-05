/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.redis.jedis.command;

import com.lubanops.stresstest.core.Tester;
import com.lubanops.stresstest.redis.RedisUtils;

/**
 * 处理redis key
 *
 * @author yiwei
 * @since 2021/11/1
 */
public interface Handler {
    byte[][] handle(byte[][] bytes);

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

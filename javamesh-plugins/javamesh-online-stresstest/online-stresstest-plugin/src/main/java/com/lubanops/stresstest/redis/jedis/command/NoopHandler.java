/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */
package com.lubanops.stresstest.redis.jedis.command;

/**
 * 不做任何处理的handler。
 *
 * @author yiwei
 * @since 2021/11/1
 */
public class NoopHandler implements Handler {
    @Override
    public byte[][] handle(byte[][] bytes) {
        return bytes;
    }
}

/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */
package com.lubanops.stresstest.redis.jedis.command;

/**
 * 不做任何处理的handler。
 *
 * @author yiwei
 * @since 2021/11/1
 */
public class ModifySkipValueHandler implements Handler {
    @Override
    public byte[][] handle(byte[][] bytes) {
        return addShadowPrefix(0, bytes.length, 2, bytes);
    }
}

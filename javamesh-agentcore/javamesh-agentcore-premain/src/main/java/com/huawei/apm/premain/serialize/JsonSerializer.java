/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.premain.serialize;

import java.nio.charset.Charset;

import com.alibaba.fastjson.JSON;

import com.huawei.apm.bootstrap.serialize.Serializer;

/**
 * Json序列化器
 * <p>不要求目标对象实现{@link java.io.Serializable}接口，但是需要提供可访问的构造函数或setter
 *
 * @author h30007557
 * @version 1.0.0
 * @since 2021/8/30
 */
public class JsonSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T t) {
        return JSON.toJSONString(t).getBytes(Charset.forName("UTF-8"));
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(new String(bytes, Charset.forName("UTF-8")), clazz);
    }
}

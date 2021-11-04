/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;

/**
 * 序列化器
 * <p>涉及到服务功能与插件间的参数或返回值类型的ClassLoader不一致时，需要通过序列化器进行转化
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/8/30
 */
public interface Serializer {
    /**
     * 序列化方法
     *
     * @param t   被序列化的对象
     * @param <T> 被序列化的对象类型
     * @return 序列化结果
     */
    <T> byte[] serialize(T t);

    /**
     * 反序列化
     *
     * @param bytes 序列化字节
     * @param clazz 目标类型Class
     * @param <T>   目标泛型
     * @return 反序列化为目标类型对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);

    /**
     * 序列化器的默认实现，使用对象流进行序列化，目标对象必须实现{@link java.io.Serializable}接口
     */
    class DefaultSerializer implements Serializer {
        /**
         * 日志
         */
        private static final Logger LOGGER = LogFactory.getLogger();

        @Override
        public <T> byte[] serialize(T t) {
            ByteArrayOutputStream bs = null;
            ObjectOutputStream os = null;
            try {
                bs = new ByteArrayOutputStream();
                os = new ObjectOutputStream(bs);
                os.writeObject(t);
                return bs.toByteArray();
            } catch (IOException ignored) {
                LOGGER.log(Level.WARNING, String.format(Locale.ROOT, "Unable to serialize [%s]. ", t));
            } finally {
                if (bs != null) {
                    try {
                        bs.close();
                    } catch (IOException ignored) {
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            return new byte[0];
        }

        @Override
        public <T> T deserialize(byte[] bytes, Class<T> clazz) {
            ObjectInputStream os = null;
            try {
                os = new ObjectInputStream(new ByteArrayInputStream(bytes));
                final Object obj = os.readObject();
                if (clazz.isAssignableFrom(obj.getClass())) {
                    return (T) obj;
                }
            } catch (IOException ignored) {
                LOGGER.log(Level.WARNING,
                        String.format(Locale.ROOT, "Unable to deserialize as [%s]. ", clazz.getName()));
            } catch (ClassNotFoundException ignored) {
                LOGGER.log(Level.WARNING,
                        String.format(Locale.ROOT, "Unable to deserialize as [%s]. ", clazz.getName()));
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            return null;
        }
    }
}

package com.huawei.javamesh.core.lubanops.bootstrap.log;

import java.util.logging.Logger;

public interface LoggerAdapter {
    /**
     * Get a logger
     *
     * @param key the returned logger will be named after clazz
     * @return logger
     */
    Logger getLogger(Class<?> key);

    /**
     * Get a logger
     *
     * @param key the returned logger will be named after key
     * @return logger
     */
    Logger getLogger(String key);

}
package com.huawei.apm.premain.lubanops.log;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.apm.core.lubanops.bootstrap.log.LogPathUtils;
import com.huawei.apm.core.lubanops.bootstrap.log.LoggerAdapter;

/**
 * @author
 * @date 2020/11/16 20:25
 */
public class JdkLoggerAdapter implements LoggerAdapter {
    /** 3个 */
    public final static int FILE_COUNT = 3;

    /** 20m */
    public final static int FILE_SIZE = 1024 * 1024 * 20;

    private FileHandler fileHandler;

    public JdkLoggerAdapter() {
        try {
            String logFilePath = LogPathUtils.getLogPath() + "logs" + File.separator;
            File fileFolder = new File(logFilePath);
            boolean result = true;
            if (!fileFolder.exists()) {
                result = fileFolder.mkdirs();
            }
            if (!result) {
                fileHandler = new FileHandler(logFilePath + "apm.log", FILE_SIZE, FILE_COUNT);
                fileHandler.setFormatter(new java.util.logging.SimpleFormatter());
            }
        } catch (Throwable t) {
            System.err.println("[APM LOG]failed to load logging.properties, cause: " + t.getMessage());
        }
    }

    @Override
    public Logger getLogger(Class<?> key) {
        Logger logger = Logger.getLogger(key.getName());
        if (fileHandler != null) {
            logger.addHandler(fileHandler);
        }
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);

        return logger;
    }

    @Override
    public Logger getLogger(String key) {
        return Logger.getLogger(key);
    }

}

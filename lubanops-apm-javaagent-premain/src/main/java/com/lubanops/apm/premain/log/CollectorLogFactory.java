package com.lubanops.apm.premain.log;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lubanops.apm.bootstrap.log.LogPathUtils;

public class CollectorLogFactory {
    private static final ConcurrentMap<String, Logger> LOGGERS = new ConcurrentHashMap<String, Logger>();

    /** 3个 */
    public static final int FILE_COUNT = 3;

    /** 20M */
    public static final int FILE_SIZE = 1024 * 1024 * 20;

    private static FileHandler fileHandler;

    // private static ConsoleHandler consoleHandler;

    static {
        try {
            String logFilePath = LogPathUtils.getLogPath() + "logs" + File.separator;
            File fileFolder = new File(logFilePath);
            boolean result = true;
            if (!fileFolder.exists()) {
                result = fileFolder.mkdirs();
            }
            if (result) {
                fileHandler = new FileHandler(logFilePath + "apm.log", FILE_SIZE, FILE_COUNT);
                fileHandler.setFormatter(new java.util.logging.SimpleFormatter());
                fileHandler.setLevel(Level.INFO);
            }
        } catch (Exception e) {
        }
    }

    public static Logger getLogger(Class<?> c) {
        return getLogger(c.getName());

    }

    public static Logger getLogger(String key) {
        Logger logger = null;
        if (null != LOGGERS.get(key)) {
            logger = LOGGERS.get(key);
        } else {
            logger = Logger.getLogger(key);
            if (fileHandler != null) {
                logger.addHandler(fileHandler);
            }
            logger.setLevel(Level.INFO);
            logger.setUseParentHandlers(false);
            LOGGERS.put(key, logger);
        }
        return logger;
    }

    public static String getFileEncoding() {
        if (fileHandler != null) {
            return fileHandler.getEncoding();
        }
        return "none";
    }
}

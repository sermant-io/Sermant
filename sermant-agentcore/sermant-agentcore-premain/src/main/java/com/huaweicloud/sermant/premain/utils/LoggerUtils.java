package com.huaweicloud.sermant.premain.utils;

import java.time.LocalDateTime;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * 日志工具类
 *
 * @author luanwenfei
 * @since 2023-07-20
 */
public class LoggerUtils {
    private static Logger logger;

    public static Logger getLogger() {
        if (null != logger) {
            return logger;
        }
        logger = Logger.getLogger("sermant.agent");
        final ConsoleHandler handler = new ConsoleHandler();
        final String lineSeparator = System.getProperty("line.separator");
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                String time = LocalDateTime.now().toString();
                return "[" + time + "] " + "[" + record.getLevel() + "] " + record.getMessage() + lineSeparator;
            }
        });
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
        return logger;
    }
}

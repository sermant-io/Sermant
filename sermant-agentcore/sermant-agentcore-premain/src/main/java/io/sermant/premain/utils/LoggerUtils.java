/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.premain.utils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Log tool class
 *
 * @author luanwenfei
 * @since 2023-07-20
 */
public class LoggerUtils {
    private static final Logger LOGGER = initLogger();

    private LoggerUtils() {
    }

    private static Logger initLogger() {
        Logger tmpLogger = Logger.getLogger("sermant.agent");
        final ConsoleHandler handler = new ConsoleHandler();
        final String lineSeparator = System.getProperty("line.separator");
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                String time = LocalDateTime.now().toString();
                return "[" + time + "] " + "[" + record.getLevel() + "] " + record.getMessage() + lineSeparator;
            }
        });
        tmpLogger.addHandler(handler);
        tmpLogger.setUseParentHandlers(false);
        return tmpLogger;
    }

    /**
     * Get logger
     *
     * @return Logger
     */
    public static Logger getLogger() {
        return LOGGER;
    }

    /**
     * record stack trace
     *
     * @param throwable throwable
     * @return record string
     */
    public static String recordStackTrace(Throwable throwable) {
        StringBuilder record = new StringBuilder(throwable.toString());
        record.append(System.lineSeparator());
        StackTraceElement[] messages = throwable.getStackTrace();
        Arrays.stream(messages).filter(Objects::nonNull).forEach(stackTraceElement -> {
            record.append(stackTraceElement).append(System.lineSeparator());
        });
        return record.toString();
    }
}

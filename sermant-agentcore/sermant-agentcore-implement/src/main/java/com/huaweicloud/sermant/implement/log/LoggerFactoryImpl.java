/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.implement.log;

import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation class for log initialization
 *
 * @author luanwenfei
 * @since 2022-06-20
 */
public class LoggerFactoryImpl {
    private static final String LOG_LEVEL_KEY = "sermant_log_level";

    private static final String INFO = "info";

    private static final String ALL = "all";

    private static final String TRACE = "trace";

    private static final String DEBUG = "debug";

    private static final String WARN = "warn";

    private static final String ERROR = "error";

    private static final String OFF = "off";

    private LoggerFactoryImpl() {
    }

    /**
     * init
     *
     * @param artifact artifact
     * @return return logger for sermant
     */
    public static Logger init(String artifact) {
        Logger logger = java.util.logging.Logger.getLogger("sermant." + artifact);
        return getLogger(logger);
    }

    private static Logger getLogger(Logger logger) {
        logger.addHandler(new SermantBridgeHandler());
        logger.setUseParentHandlers(false);
        logger.setLevel(getLevel());
        return logger;
    }

    private static Level getLevel() {
        // Environment Variables > Startup Parameters
        String level = System.getenv(LOG_LEVEL_KEY);
        if (StringUtils.isBlank(level)) {
            level = System.getProperty(LOG_LEVEL_KEY, INFO);
        }
        level = level.toLowerCase(Locale.ROOT);
        switch (level) {
            case ALL:
                return Level.ALL;
            case TRACE:
                return Level.FINEST;
            case DEBUG:
                return Level.FINE;
            case WARN:
                return Level.WARNING;
            case ERROR:
                return Level.SEVERE;
            case OFF:
                return Level.OFF;
            default:
                return Level.INFO;
        }
    }
}
/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.premain;

import com.huawei.sermant.premain.common.BootArgsBuilder;
import com.huawei.sermant.premain.common.PathDeclarer;
import com.huawei.sermant.premain.exception.DupPremainException;

import com.huaweicloud.sermant.core.AgentCoreEntrance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.BindException;
import java.security.acl.NotOwnerException;
import java.sql.SQLException;
import java.util.ConcurrentModificationException;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.jar.JarException;
import java.util.jar.JarFile;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.naming.InsufficientResourcesException;
/**
 * Agent Premain方法
 *
 * @author luanwenfei
 * @since 2022-03-26
 */
public class AgentPremain {
    private static boolean executeFlag = false;

    private static final Logger LOGGER = getLogger();

    private AgentPremain() {
    }

    /**
     * premain
     *
     * @param agentArgs agentArgs
     * @param instrumentation instrumentation
     * @throws DupPremainException
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        try {
            // 执行标记，防止重复运行
            if (executeFlag) {
                throw new DupPremainException();
            }
            executeFlag = true;

            // 添加核心库
            LOGGER.info("Loading core library... ");
            loadCoreLib(instrumentation);

            // 初始化启动参数
            LOGGER.info("Building argument map... ");
            final Map<String, Object> argsMap = BootArgsBuilder.build(agentArgs);

            // agent core入口
            LOGGER.info("Loading sermant agent... ");
            AgentCoreEntrance.run(argsMap, instrumentation);

            LOGGER.info("Load sermant done. ");
        } catch (FileNotFoundException | OutOfMemoryError | StackOverflowError | MissingResourceException
                 | NotOwnerException | JarException | ConcurrentModificationException | BindException
                 | InsufficientResourcesException | SQLException e) {
            LOGGER.severe("Loading sermant agent failed. ");
        } catch (Exception e) {
            LOGGER.severe(
                String.format(Locale.ROOT, "Loading sermant agent failed, %s. ", e));
        }
    }

    private static void loadCoreLib(Instrumentation instrumentation) throws IOException {
        final File coreDir = new File(PathDeclarer.getCorePath());
        if (!coreDir.exists() || !coreDir.isDirectory()) {
            throw new RuntimeException("core directory is not exist or is not directory.");
        }
        final File[] jars = coreDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        if (jars == null || jars.length <= 0) {
            throw new RuntimeException("core directory is empty");
        }
        for (File jar : jars) {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(jar);
                instrumentation.appendToSystemClassLoaderSearch(jarFile);
            } finally {
                if (jarFile != null) {
                    try {
                        jarFile.close();
                    } catch (IOException ignored) {
                        LOGGER.severe(ignored.getMessage());
                    }
                }
            }
        }
    }

    private static Logger getLogger() {
        final Logger logger = Logger.getLogger("sermant.agent");
        final ConsoleHandler handler = new ConsoleHandler();
        final String lineSeparator = System.getProperty("line.separator");
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return "[" + record.getLevel() + "] " + record.getMessage() + lineSeparator;
            }
        });
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
        return logger;
    }
}

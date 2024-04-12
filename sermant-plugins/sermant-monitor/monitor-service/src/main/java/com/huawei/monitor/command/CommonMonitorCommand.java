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

package com.huawei.monitor.command;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.utils.StringUtils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * result parsing abstract class
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 * @param <T> analysis result
 */
public abstract class CommonMonitorCommand<T> implements MonitorCommand<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int MAX_LOG_LENGTH = 200;

    private static final String ELLIPSIS = "...";

    /**
     * result of executing the command
     *
     * @param inputStream input stream
     * @return commandResult
     */
    protected List<String> readLines(InputStream inputStream) {
        try {
            return IOUtils.readLines(inputStream, String.valueOf(Charset.defaultCharset()));
        } catch (IOException e) {
            LOGGER.severe(String.format("Failed to parse input from subprocess caused by: %s",
                    e.getMessage()));
        }
        return Collections.emptyList();
    }

    /**
     * error preprocessing
     *
     * @param errorStream external process error stream
     */
    @Override
    public void handleError(InputStream errorStream) {
        final List<String> lines = readLines(errorStream);
        StringBuilder outputBuilder = new StringBuilder();
        int totalLength = 0;
        for (String line : lines) {
            int length = line.length();
            int newTotalLength = totalLength + length;
            if (newTotalLength > MAX_LOG_LENGTH) {
                outputBuilder.append(line, 0, newTotalLength - MAX_LOG_LENGTH)
                        // if end with punctuation, should remove it first?
                        .append(ELLIPSIS);
                break;
            } else {
                outputBuilder.append(line);
                totalLength = newTotalLength;
            }
        }
        if (!StringUtils.isBlank(outputBuilder.toString())) {
            LOGGER.severe("Subprocess error: " + outputBuilder);
        }
    }
}

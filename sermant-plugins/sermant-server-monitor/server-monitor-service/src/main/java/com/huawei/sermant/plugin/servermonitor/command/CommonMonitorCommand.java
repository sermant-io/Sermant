/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.plugin.servermonitor.command;

import com.huawei.sermant.core.common.LoggerFactory;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static com.huawei.sermant.plugin.monitor.common.utils.CommonUtil.getStackTrace;

/**
 * 使用通用错误处理方式的{@link MonitorCommand}，即通过日志输出错误信息，当错误
 * 信息的长度超过200时，则忽略超过该长度的内容，并在日志内容结尾添加省略号。
 *
 * @param <T>
 */
public abstract class CommonMonitorCommand<T> implements MonitorCommand<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int MAX_LOG_LENGTH = 200;

    private static final String ELLIPSIS = "...";

    protected List<String> readLines(InputStream inputStream) {
        try {
            return IOUtils.readLines(inputStream, Charset.defaultCharset());
        } catch (IOException e) {
            LOGGER.severe(String.format("Failed to parse input from subprocess caused by: %s",
                getStackTrace(e)));
        }
        return Collections.emptyList();
    }

    @Override
    public void handleError(InputStream errorStream) {
        final List<String> lines = readLines(errorStream);
        StringBuilder outputBuilder = new StringBuilder("Subprocess error: ");
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
        LOGGER.severe(outputBuilder.toString());
    }
}

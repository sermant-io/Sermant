/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.servermonitor.command;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * 使用通用错误处理方式的{@link MonitorCommand}，即通过日志输出错误信息，当错误
 * 信息的长度超过200时，则忽略超过该长度的内容，并在日志内容结尾添加省略号。
 *
 * @param <T>
 */
public abstract class CommonMonitorCommand<T> implements MonitorCommand<T> {

    private static final Logger LOGGER = LogFactory.getLogger();

    private static final int MAX_LOG_LENGTH = 200;

    private static final String ELLIPSIS = "...";

    protected List<String> readLines(InputStream inputStream) {
        try {
            return IOUtils.readLines(inputStream, Charset.defaultCharset());
        } catch (IOException e) {
            // LOG "Failed to parse input from subprocess."
        }
        return Collections.emptyList();
    }

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
        LOGGER.severe(outputBuilder.toString());
    }
}

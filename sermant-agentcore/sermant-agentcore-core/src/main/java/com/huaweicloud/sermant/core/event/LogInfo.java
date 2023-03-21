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

package com.huaweicloud.sermant.core.event;

import java.util.Objects;
import java.util.logging.LogRecord;

/**
 * 日志信息
 *
 * @author luanwenfei
 * @since 2023-03-08
 */
public class LogInfo {
    private String logLevel;

    private String logMessage;

    private String logClass;

    private String logMethod;

    private int logThreadId;

    private Throwable throwable;

    /**
     * 通过LogRecord构造日志事件信息
     *
     * @param logRecord logRecord
     */
    public LogInfo(LogRecord logRecord) {
        this.logLevel = logRecord.getLevel().getName();
        this.logMessage = logRecord.getMessage();
        this.logClass = logRecord.getSourceClassName();
        this.logMethod = logRecord.getSourceMethodName();
        this.logThreadId = logRecord.getThreadID();
        this.throwable = logRecord.getThrown();
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public String getLogClass() {
        return logClass;
    }

    public void setLogClass(String logClass) {
        this.logClass = logClass;
    }

    public String getLogMethod() {
        return logMethod;
    }

    public void setLogMethod(String logMethod) {
        this.logMethod = logMethod;
    }

    public int getLogThreadId() {
        return logThreadId;
    }

    public void setLogThreadId(int logThreadId) {
        this.logThreadId = logThreadId;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public String toString() {
        return "LogInfo{" + "logLevel='" + logLevel + '\'' + ", logMessage='" + logMessage + '\'' + ", logClass='"
                + logClass + '\'' + ", logMethod='" + logMethod + '\'' + ", logThreadId='" + logThreadId + '\''
                + ", throwable=" + throwable + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LogInfo logInfo = (LogInfo) obj;
        return logThreadId == logInfo.logThreadId && Objects.equals(logLevel, logInfo.logLevel)
                && Objects.equals(logMessage, logInfo.logMessage) && Objects.equals(logClass,
                logInfo.logClass) && Objects.equals(logMethod, logInfo.logMethod) && Objects.equals(
                throwable, logInfo.throwable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logLevel, logMessage, logClass, logMethod, logThreadId, throwable);
    }
}

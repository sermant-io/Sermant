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

package com.huawei.monitor.command;

import java.io.InputStream;

/**
 * linux monitoring command processing interface
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 * @param <T> analysis result
 */
public interface MonitorCommand<T> {

    /**
     * get linux command
     *
     * @return linux command
     */
    String getCommand();

    /**
     * Result parsing, parsing the output stream of external processes
     *
     * @param inputStream external process output stream
     * @return the result after analysis
     */
    T parseResult(InputStream inputStream);

    /**
     * Error handling, handling error streams from external processes
     *
     * @param errorStream external process error stream
     */
    void handleError(InputStream errorStream);
}

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

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Linux命令执行器
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-08-02
 */
public class CommandExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final Runtime RUNTIME = Runtime.getRuntime();

    private static final ExecutorService POOL = new ThreadPoolExecutor(2, 10, 0, TimeUnit.SECONDS,
            new SynchronousQueue<>());

    private CommandExecutor() {
    }

    /**
     * 命令执行
     *
     * @param command 命令
     * @param <T> 结果信息
     * @return 执行结果
     */
    public static <T> Optional<T> execute(final MonitorCommand<T> command) {
        final Process process;
        try {
            String[] commands = new String[]{"/bin/sh", "-c", command.getCommand()};
            process = RUNTIME.exec(commands);
        } catch (IOException e) {
            LOGGER.severe("Failed to execute command, " + e.getMessage());
            return Optional.empty();
        }
        final InputStream errorStream = process.getErrorStream();
        final InputStream inputStream = process.getInputStream();
        try {
            CountDownLatch downLatch = new CountDownLatch(1);
            handleErrorStream(command, errorStream, downLatch);
            Future<T> parseFuture = parseResult(command, inputStream);
            process.waitFor();
            downLatch.await();
            return Optional.of(parseFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.severe("Failed to parse result, " + e.getMessage());
        } finally {
            closeStream(errorStream);
            closeStream(inputStream);
        }
        return Optional.empty();
    }

    /**
     * 关闭流
     *
     * @param inputStream 读入流
     */
    private static void closeStream(InputStream inputStream) {
        try {
            inputStream.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Fail to close stream, exception", e);
        }
    }

    /**
     * 结果解析
     *
     * @param command 命令
     * @param inputStream 读流
     * @param <T> 结果信息
     * @return 执行结果
     */
    private static <T> Future<T> parseResult(final MonitorCommand<T> command, final InputStream inputStream) {
        return POOL.submit(new InputHandleTask<>(command::parseResult, inputStream));
    }

    /**
     * 错误解析
     *
     * @param command 命令
     * @param errorStream 错误流
     * @param latch 定时任务
     * @param <T> 结果
     */
    private static <T> void handleErrorStream(final MonitorCommand<T> command, final InputStream errorStream,
            final CountDownLatch latch) {
        POOL.execute(new ErrorHandleTask(inputStream -> {
            command.handleError(inputStream);
            latch.countDown();
        }, errorStream));
    }

    /**
     * 对任务
     *
     * @param <T> 命令解析结果
     * @since 2022-08-02
     */
    private static class InputHandleTask<T> implements Callable<T> {
        private final StreamHandler<T> handler;

        private final InputStream inputStream;

        /**
         * 构造
         *
         * @param handler 前置处理
         * @param inputStream 读入
         */
        InputHandleTask(StreamHandler<T> handler, InputStream inputStream) {
            this.handler = handler;
            this.inputStream = inputStream;
        }

        @Override
        public T call() {
            return handler.handle(inputStream);
        }
    }

    /**
     * 错误前置任务
     *
     * @since 2022-08-02
     */
    private static class ErrorHandleTask implements Runnable {
        private final VoidStreamHandler handler;

        private final InputStream inputStream;

        /**
         * 构造方法
         *
         * @param handler 前置处理
         * @param inputStream 流
         */
        ErrorHandleTask(VoidStreamHandler handler, InputStream inputStream) {
            this.handler = handler;
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            handler.handle(inputStream);
        }
    }

    /**
     * Stream 前置处理
     *
     * @param <R> 解析结果
     * @since 2022-08-02
     */
    private interface StreamHandler<R> {
        R handle(InputStream inputStream);
    }

    /**
     * 前置处理
     *
     * @since 2022-08-02
     */
    private interface VoidStreamHandler {
        /**
         * 前置处理
         *
         * @param inputStream 流
         */
        void handle(InputStream inputStream);
    }
}

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
import com.huawei.sermant.plugin.servermonitor.common.StreamHandler;
import com.huawei.sermant.plugin.servermonitor.common.VoidStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Linux指令执行器
 *
 * <p>通过调用{@link #execute(MonitorCommand)}方法来执行Linux指令，该方法将
 * 调用{@link Runtime#exec(String)}方法调用外部进程来执行{@link MonitorCommand#getCommand()}
 * 方法提供的Linux指令，并通过{@link MonitorCommand#parseResult(InputStream)}和
 * {@link MonitorCommand#handleError(InputStream)}来处理外部进程的输出流和错误流，
 * 外部进程的输出流的处理结果作为方法的结果。</p>
 *
 * <p>重构泛PaaS：com.huawei.sermant.plugin.collection.util.RunLinuxCommand</p>
 */
public class CommandExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final Runtime RUNTIME = Runtime.getRuntime();

    private static final ExecutorService POOL = new ThreadPoolExecutor(2, 10,
            0, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

    public static <T> T execute(final MonitorCommand<T> command) {
        final Process process;
        try {
            process = RUNTIME.exec(command.getCommand());
        } catch (IOException e) {
            LOGGER.severe("Failed to execute command, " + e.getMessage());
            // JDK6 用不了Optional
            return null;
        }

        final InputStream errorStream = process.getErrorStream();
        final InputStream inputStream = process.getInputStream();
        try {
            CountDownLatch downLatch = new CountDownLatch(1);
            handleErrorStream(command, errorStream, downLatch);
            Future<T> parseFuture = parseResult(command, inputStream);
            // JDK6 无法超时等待
            process.waitFor();
            downLatch.await();
            return parseFuture.get();
            // Should destroy the subprocess when timout? JDK6 也用不了
        } catch (InterruptedException e) {
            // Ignored.
        } catch (ExecutionException e) {
            LOGGER.severe("Failed to parse result, " + e.getMessage());
        } finally {
            closeStream(errorStream);
            closeStream(inputStream);
        }
        return null;
    }

    private static void closeStream(InputStream inputStream) {
        try {
            inputStream.close();
        } catch (IOException e) {
            // ignored
        }
    }

    private static <T> Future<T> parseResult(final MonitorCommand<T> command, final InputStream inputStream) {
        return POOL.submit(new InputHandleTask<T>(new StreamHandler<T>() {
            @Override
            public T handle(InputStream inputStream) {
                return command.parseResult(inputStream);
            }
        }, inputStream));
    }

    private static <T> void handleErrorStream(
            final MonitorCommand<T> command, final InputStream errorStream, final CountDownLatch latch) {
        POOL.execute(new ErrorHandleTask(new VoidStreamHandler() {
            @Override
            public void handle(InputStream inputStream) {
                command.handleError(inputStream);
                latch.countDown();
            }
        }, errorStream));
    }

    private static class InputHandleTask<T> implements Callable<T> {

        private final StreamHandler<T> handler;

        private final InputStream inputStream;

        public InputHandleTask(StreamHandler<T> handler, InputStream inputStream) {
            this.handler = handler;
            this.inputStream = inputStream;
        }

        @Override
        public T call() {
            return handler.handle(inputStream);
        }
    }

    private static class ErrorHandleTask implements Runnable {

        private final VoidStreamHandler handler;

        private final InputStream inputStream;

        public ErrorHandleTask(VoidStreamHandler handler, InputStream inputStream) {
            this.handler = handler;
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            handler.handle(inputStream);
        }
    }

}

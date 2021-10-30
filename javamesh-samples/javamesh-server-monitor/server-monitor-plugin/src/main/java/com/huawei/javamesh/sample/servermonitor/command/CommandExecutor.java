/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.servermonitor.command;

import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.javamesh.sample.servermonitor.common.Consumer;
import com.huawei.javamesh.sample.servermonitor.common.Function;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
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
 * <p>重构泛PaaS：com.huawei.apm.plugin.collection.util.RunLinuxCommand</p>
 */
public class CommandExecutor {

    private static final Logger LOGGER = LogFactory.getLogger();

    private static final Runtime RUNTIME = Runtime.getRuntime();

    private static final ExecutorService POOL = new ThreadPoolExecutor(2, 2,
        0, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

    //private static final int EXEC_TIMEOUT_SECONDS = 60;

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
            handleErrorStream(command, errorStream);
            Future<T> parseFuture = parseResult(command, inputStream);
            // JDK6 无法超时等待
            // process.waitFor(EXEC_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            process.waitFor();
            return parseFuture.get();
            // LOGGER.warn("timeout.")
            // Should destroy the subprocess when timout? JDK6 也用不了
            // process.destroyForcibly();
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
        return POOL.submit(new InputHandleTask<T>(new Function<InputStream, T>() {
            @Override
            public T apply(InputStream inputStream) {
                return command.parseResult(inputStream);
            }
        }, inputStream));
    }

    private static <T> void handleErrorStream(final MonitorCommand<T> command, final InputStream errorStream) {
        POOL.execute(new ErrorHandleTask(new Consumer<InputStream>() {
            @Override
            public void accept(InputStream inputStream) {
                command.handleError(inputStream);
            }
        }, errorStream));
    }

    private static class InputHandleTask<T> implements Callable<T> {

        private final Function<InputStream, T> function;

        private final InputStream inputStream;

        public InputHandleTask(Function<InputStream, T> function, InputStream inputStream) {
            this.function = function;
            this.inputStream = inputStream;
        }

        @Override
        public T call() {
            return function.apply(inputStream);
        }
    }

    private static class ErrorHandleTask implements Runnable {

        private final Consumer<InputStream> consumer;

        private final InputStream inputStream;

        public ErrorHandleTask(Consumer<InputStream> consumer, InputStream inputStream) {
            this.consumer = consumer;
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            consumer.accept(inputStream);
        }
    }

}

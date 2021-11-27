/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec.executor;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.huawei.script.exec.ExecResult;
import com.huawei.script.exec.log.LogCallBack;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;

/**
 * 用于本地linux服务器上执行shell脚本
 *
 * @author y30010171
 * @since 2021-10-20
 **/
@Component
public class LocalScriptExecutor implements ScriptExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalScriptExecutor.class);

    //private static final String SH = "/bin/sh";
    private static final String SH = "cmd";
    //private static final String SH_C = "-C";
    private static final String SH_C = "/C";

    @Value("${script.location}")
    private String scriptLocation = "/tmp/";

    @Override
    public String mode() {
        return LOCAL;
    }

    @Override
    public ExecResult execScript(ScriptExecInfo scriptExecInfo, LogCallBack logCallback) {
        String fileName = "";
        try {
            fileName = createScriptFile(scriptExecInfo.getScriptName(), scriptExecInfo.getScriptContext());
            return exec(commands(fileName, scriptExecInfo.getParams()), logCallback);
        } catch (FileNotFoundException e) {
            return ExecResult.fail("Please check out your scriptLocation.");
        } catch (IOException e) {
            LOGGER.error("Failed to create local script.", e);
            return ExecResult.fail(e.getMessage());
        } finally {
            if (StringUtils.isNotEmpty(fileName)) {
                File file = new File(fileName);
                if (file.exists() && file.delete()) {
                    LOGGER.info("script file {} was deleted.", fileName);
                }
            }
        }
    }

    private String createScriptFile(String scriptName, String scriptContent) throws IOException {
        String fileName = String.format(Locale.ROOT, "%s%s-%s.bat",
            scriptLocation, scriptName, System.nanoTime());
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            fileOutputStream.write(("echo $$" + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
            fileOutputStream.write(scriptContent.getBytes(StandardCharsets.UTF_8));
            fileOutputStream.flush();
            LOGGER.info("script file {} was created.", fileName);
        }
        return fileName;
    }

    private ExecResult exec(String[] commands, LogCallBack logCallback) {
        try {
            Process exec = Runtime.getRuntime().exec(commands);
            String info = parseResult(exec.getInputStream(), logCallback);
            String errorInfo = parseResult(exec.getErrorStream(), logCallback);
            return exec.waitFor() == 0 ? ExecResult.success(info) : ExecResult.fail(errorInfo);
        } catch (IOException | InterruptedException e) {
            return ExecResult.fail(e.getMessage());
        }
    }

    /**
     * 解析输入流中的消息
     *
     * @param inputStream
     * @return String 结果
     * @throws IOException
     */
    private String parseResult(InputStream inputStream, LogCallBack logCallback) throws IOException {
        StringBuilder result = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "GBK"));
        String lines;
        while ((lines = bufferedReader.readLine()) != null) {
            if (logCallback != null) {
                logCallback.handle(lines);
            }
            result.append(lines).append(System.lineSeparator());
        }
        return result.toString();
    }

    private String[] commands(String command, String[] params) {
        return (String[]) ArrayUtils.addAll(new String[]{SH, SH_C, command}, params);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        LocalScriptExecutor localScriptExecutor = new LocalScriptExecutor();
        ScriptExecInfo script10 = new ScriptExecInfo();
        script10.setScriptName("test-error");
        script10.setScriptContext("ping 127.0.0.1 -n 10");
        ExecutorService executorService = Executors.newFixedThreadPool(4, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                //thread.setDaemon(true);
                return thread;
            }
        });
        final ListeningExecutorService listeningExecutorService = MoreExecutors.listeningDecorator(executorService);
        long start = System.currentTimeMillis();
        ListenableFuture<ExecResult> submit1 = listeningExecutorService.submit(() -> localScriptExecutor.execScript(script10, null));
        ListenableFuture<ExecResult> submit2 = listeningExecutorService.submit(() -> localScriptExecutor.execScript(script10, null));
        ListenableFuture<ExecResult> submit3 = listeningExecutorService.submit(() -> localScriptExecutor.execScript(script10, null));
        ListenableFuture<ExecResult> submit4 = listeningExecutorService.submit(() -> localScriptExecutor.execScript(script10, null));
        FutureCallback<ExecResult> callback = new FutureCallback<ExecResult>() {
            @Override
            public void onSuccess(@Nullable ExecResult result) {
                LOGGER.info("cost {} ms", System.currentTimeMillis() - start);
                System.out.println(result);
            }

            @Override
            public void onFailure(Throwable t) {
                LOGGER.error("cost {} ms", System.currentTimeMillis() - start);
            }
        };
        //List<ExecResult> execResults = listListenableFuture.get(2,TimeUnit.SECONDS);
        AtomicLong timeOut = new AtomicLong(2000L);
        Arrays.asList(submit1,submit2,submit3,submit4).forEach( submit ->{
            try {
                Futures.addCallback(submit, callback);
                submit.get(timeOut.get(),TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                submit.cancel(true);
                timeOut.set(100L);
                e.printStackTrace();
            }
        });
        LOGGER.info("cost {} ms", System.currentTimeMillis() - start);
    }
}

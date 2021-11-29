/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.script.exec.executor;

import com.huawei.script.exec.ExecResult;
import com.huawei.script.exec.log.LogCallBack;

import com.huawei.script.exec.session.ServerInfo;
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
import java.util.Locale;

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
    //private static final String SH_C = "-C";
    private static final String SH = "cmd";
    private static final String SH_C = "/c";

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
            return exec(commands(fileName, scriptExecInfo.getParams()), logCallback, scriptExecInfo.getId());
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

    @Override
    public ExecResult cancel(ServerInfo serverInfo, int pid) {
        return exec(commands(String.format(Locale.ROOT, "kill -9 %s", pid), null), null, -1);
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

    private ExecResult exec(String[] commands, LogCallBack logCallback, int id) {
        try {
            Process exec = Runtime.getRuntime().exec(commands);
            String info = parseResult(exec.getInputStream(), logCallback, id);
            String errorInfo = parseResult(exec.getErrorStream(), logCallback, id);
            return exec.waitFor() == 0 ? ExecResult.success(info) : ExecResult.fail(errorInfo);
        } catch (IOException | InterruptedException e) {
            return ExecResult.fail(e.getMessage());
        }
    }

    /**
     * 解析输入流中的消息
     *
     * @param inputStream 输入流
     * @param logCallback 日志回调
     * @param id          标识本次执行的关键字
     * @return String 结果
     * @throws IOException
     */
    private String parseResult(InputStream inputStream, LogCallBack logCallback, int id) throws IOException {
        StringBuilder result = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "GBK"));
        String lines;
        boolean readFirstLogAsPid = true;
        while ((lines = bufferedReader.readLine()) != null) {
            if (logCallback != null && id > 0) {
                if (readFirstLogAsPid) {
                    logCallback.handlePid(id, lines);
                    readFirstLogAsPid = false;
                    continue;
                } else {
                    logCallback.handleLog(id, lines);
                }
            }
            result.append(lines).append(System.lineSeparator());
        }
        return result.toString();
    }

    private String[] commands(String command, String[] params) {
        return (String[]) ArrayUtils.addAll(new String[]{SH, SH_C, command}, params);
    }
}

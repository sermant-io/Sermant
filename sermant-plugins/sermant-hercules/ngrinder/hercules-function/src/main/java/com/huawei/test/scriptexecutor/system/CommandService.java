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

package com.huawei.test.scriptexecutor.system;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 功能描述：执行linux命令服务
 * 执行命令结果都是封装到{@link ExecuteInfo}
 * 自定义了三种返回状态码：
 * {@link CommandService#INVALID_PARAM_EXIT_VALUE} : 命令执行方法调用时，参数不合法状态码
 * {@link CommandService#INVALID_COMMAND_EXIT_VALUE} : 执行命令不合法时，返回的状态码
 * {@link CommandService#INTERRUPTED_EXIT_VALUE} : 执行子进程被打断时，返回的状态码
 *
 * @author zl
 * @since 2021-12-21
 */
public class CommandService {
    /**
     * 命令执行方法调用时，参数不合法状态码
     */
    public static final int INVALID_PARAM_EXIT_VALUE = -1;

    /**
     * 执行命令不合法时，返回的状态码
     */
    public static final int INVALID_COMMAND_EXIT_VALUE = -2;

    /**
     * 执行子进程被打断时，返回的状态码
     */
    public static final int INTERRUPTED_EXIT_VALUE = -3;

    /**
     * 外部进程正常输出流处理线程名称
     */
    private static final String INPUT_STREAM_HANDLER_THREAD = "input-stream-handler-thread";

    /**
     * 外部进程错误输出流处理线程名称
     */
    private static final String ERROR_STREAM_HANDLER_THREAD = "error-stream-handler-thread";

    /**
     * 日志工具
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandService.class);

    /**
     * 直接执行string命令，内部调用重载方法方法:
     * {@link CommandService#doExecute(String, String[], File, long)}
     * 自定义了三种返回状态码：
     * {@link CommandService#INVALID_PARAM_EXIT_VALUE} : 命令执行方法调用时，参数不合法状态码
     * {@link CommandService#INVALID_COMMAND_EXIT_VALUE} : 执行命令不合法时，返回的状态码
     * {@link CommandService#INTERRUPTED_EXIT_VALUE} : 执行子进程被打断时，返回的状态码
     *
     * @param commandString    命令字符串
     * @param waitTimeMillions 等待子进程执行的时间, 如果传入0表示一直等待阻塞方法执行完毕
     * @return 命令执行信息
     */
    public ExecuteInfo execCommand(String commandString, long waitTimeMillions) {
        return doExecute(commandString, null, null, waitTimeMillions);
    }

    /**
     * 直接执行string命令，内部调用方法
     * {@link CommandService#doExecute(String, String[], File, long)}
     * 自定义了三种返回状态码：
     * {@link CommandService#INVALID_PARAM_EXIT_VALUE} : 命令执行方法调用时，参数不合法状态码
     * {@link CommandService#INVALID_COMMAND_EXIT_VALUE} : 执行命令不合法时，返回的状态码
     * {@link CommandService#INTERRUPTED_EXIT_VALUE} : 执行子进程被打断时，返回的状态码
     *
     * @param commandString    命令字符串
     * @param envProperties    执行命令需要使用的参数
     * @param waitTimeMillions 等待子进程执行的时间, 如果传入0表示一直等待阻塞方法执行完毕
     * @return 命令执行信息
     */
    public ExecuteInfo execCommand(String commandString, String[] envProperties, long waitTimeMillions) {
        return doExecute(commandString, envProperties, null, waitTimeMillions);
    }

    /**
     * 直接执行string命令，内部调用方法
     * {@link CommandService#doExecute(String, String[], File, long)}
     * 自定义了三种返回状态码：
     * {@link CommandService#INVALID_PARAM_EXIT_VALUE} : 命令执行方法调用时，参数不合法状态码
     * {@link CommandService#INVALID_COMMAND_EXIT_VALUE} : 执行命令不合法时，返回的状态码
     * {@link CommandService#INTERRUPTED_EXIT_VALUE} : 执行子进程被打断时，返回的状态码
     *
     * @param commandString    命令字符串
     * @param envProperties    执行命令需要使用的参数
     * @param homeDir          命令执行主目录
     * @param waitTimeMillions 等待子进程执行的时间, 如果传入0表示一直等待阻塞方法执行完毕
     * @return 命令执行信息
     */
    public ExecuteInfo execCommand(String commandString, String[] envProperties, File homeDir, long waitTimeMillions) {
        return doExecute(commandString, envProperties, homeDir, waitTimeMillions);
    }

    /**
     * 直接执行string命令 <br />
     * 自定义了三种返回状态码：<br />
     * {@link CommandService#INVALID_PARAM_EXIT_VALUE} : 命令执行方法调用时，参数不合法状态码 <br />
     * {@link CommandService#INVALID_COMMAND_EXIT_VALUE} : 执行命令不合法时，返回的状态码 <br />
     * {@link CommandService#INTERRUPTED_EXIT_VALUE} : 执行子进程被打断时，返回的状态码 <br />
     *
     * @param commands         命令集
     * @param waitTimeMillions 等待子进程执行的时间, 如果传入0表示一直等待阻塞方法执行完毕
     * @return 命令执行信息
     */
    public ExecuteInfo execCommand(String[] commands, long waitTimeMillions) {
        return doExecute(commands, null, null, waitTimeMillions);
    }

    /**
     * 直接执行string命令
     *
     * @param commands         命令集
     * @param envProperties    命令执行参数
     * @param waitTimeMillions 等待子进程执行的时间, 如果传入0表示一直等待阻塞方法执行完毕
     * @return 命令执行信息
     */
    public ExecuteInfo execCommand(String[] commands, String[] envProperties, long waitTimeMillions) {
        return doExecute(commands, envProperties, null, waitTimeMillions);
    }

    /**
     * 直接执行string命令，内部调用方法{@link Runtime#exec(String[])}
     *
     * @param commands         命令集
     * @param envProperties    命令执行参数
     * @param homeDir          命令执行主目录
     * @param waitTimeMillions 等待子进程执行的时间, 如果传入0表示一直等待阻塞方法执行完毕
     * @return 命令执行信息
     */
    public ExecuteInfo execCommand(String[] commands, String[] envProperties, File homeDir, long waitTimeMillions) {
        return doExecute(commands, envProperties, homeDir, waitTimeMillions);
    }

    /**
     * 字符串命令执行方式
     *
     * @param commandString    字符串命令
     * @param envProperties    命令执行环境参数
     * @param homeDir          命令执行主目录
     * @param waitTimeMillions 命令执行等待时间
     * @return 命令执行结果信息
     */
    private ExecuteInfo doExecute(String commandString, String[] envProperties, File homeDir, long waitTimeMillions) {
        if (StringUtils.isEmpty(commandString)) {
            LOGGER.error("Execute command error, command is empty.");
            return new ExecuteInfo("", "The command can not be empty.", INVALID_PARAM_EXIT_VALUE);
        }
        Process process = null;
        try {
            LOGGER.debug("Execute command:{}.", commandString);
            process = Runtime.getRuntime().exec(commandString, envProperties, homeDir);
            return handleProcessStream(process, waitTimeMillions);
        } catch (IOException e) {
            LOGGER.warn("Command error: command:{}", commandString, e);
            return new ExecuteInfo("", e.getMessage(), INVALID_COMMAND_EXIT_VALUE);
        } finally {
            destroyProcess(process);
        }
    }

    /**
     * 命令数组执行方式
     *
     * @param commands         字符串命令数组
     * @param envProperties    命令执行环境参数
     * @param homeDir          命令执行主目录
     * @param waitTimeMillions 命令执行等待时间
     * @return 命令执行结果信息
     */
    private ExecuteInfo doExecute(String[] commands, String[] envProperties, File homeDir, long waitTimeMillions) {
        if (commands == null || commands.length == 0) {
            LOGGER.warn("Execute command error, command array is null or empty.");
            return new ExecuteInfo("", "The command array is invalid.", INVALID_PARAM_EXIT_VALUE);
        }
        Process process = null;
        try {
            LOGGER.info("Execute commands:{}.", Arrays.asList(commands));
            process = Runtime.getRuntime().exec(commands, envProperties, homeDir);
            return handleProcessStream(process, waitTimeMillions);
        } catch (IOException e) {
            LOGGER.warn("Command error:{}, command:{}.", e.getMessage(), Arrays.asList(commands));
            return new ExecuteInfo("", e.getMessage(), INVALID_COMMAND_EXIT_VALUE);
        } finally {
            destroyProcess(process);
        }
    }

    /**
     * 处理linux命令执行过程中，外部进程产生的输出和错误信息，封装到{@link ExecuteInfo}返回
     *
     * @param waitTimeMillions 等待子进程执行的时间
     * @param process          需要处理的执行线程
     * @return 封装到{@link ExecuteInfo}实例
     */
    private ExecuteInfo handleProcessStream(Process process, long waitTimeMillions) {
        if (process == null) {
            return new ExecuteInfo("", "Process is null.", INVALID_PARAM_EXIT_VALUE);
        }
        LOGGER.debug("Start to handle input or error stream for process.");
        StreamGobbler inputSteamGobbler = new StreamGobbler(INPUT_STREAM_HANDLER_THREAD, process.getInputStream());
        StreamGobbler errorSteamGobbler = new StreamGobbler(ERROR_STREAM_HANDLER_THREAD, process.getErrorStream());
        inputSteamGobbler.start();
        errorSteamGobbler.start();
        try {
            int exitValue;

            // 如果传入的等待时间大于0，就只等待设置的时间，如果小于等于0，则一直等待子进程执行结束
            if (waitTimeMillions > 0) {
                process.waitFor(waitTimeMillions, TimeUnit.MILLISECONDS);
                exitValue = process.exitValue();

                // 避免调用线程在信息处理线程处理完毕之前就执行完了
                inputSteamGobbler.join(waitTimeMillions);
                errorSteamGobbler.join(waitTimeMillions);
            } else {
                exitValue = process.waitFor();

                // 避免调用线程在信息处理线程处理完毕之前就执行完了
                inputSteamGobbler.join();
                errorSteamGobbler.join();
            }
            String inputStreamInfo = inputSteamGobbler.getExecuteInfo(waitTimeMillions);
            String errorStreamInfo = errorSteamGobbler.getExecuteInfo(waitTimeMillions);
            return new ExecuteInfo(inputStreamInfo, errorStreamInfo, exitValue);
        } catch (InterruptedException | IllegalThreadStateException e) {
            LOGGER.warn("The sub process is interrupted, reason:{}", e.getMessage());
            return new ExecuteInfo("", e.getMessage(), INTERRUPTED_EXIT_VALUE);
        }
    }

    /**
     * 终止子进程的执行
     *
     * @param process 执行的线程
     */
    private void destroyProcess(Process process) {
        if (process == null) {
            return;
        }
        LOGGER.debug("Start to destroy process...");
        process.destroyForcibly();
    }
}

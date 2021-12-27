package com.huawei.test.scriptexecutor.system;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Runtime.class})
public class CommandServiceTest {
    /**
     * CommandService#execCommand(java.lang.String, java.lang.String[], java.io.File, long)
     */
    @Test
    public void test_execCommand_by_one_command_when_command_is_empty() {
        CommandService commandService = new CommandService();
        ExecuteInfo executeInfo = commandService.execCommand("", null, new File("/"), 1000);
        Assert.assertEquals(CommandService.INVALID_PARAM_EXIT_VALUE, executeInfo.getExitValue());
    }

    /**
     * CommandService#execCommand(java.lang.String, java.lang.String[], java.io.File, long)
     */
    @Test
    public void test_ExecCommand_by_one_command_when_command_throw_ioException() {
        CommandService commandService = new CommandService();
        ExecuteInfo executeInfo = commandService.execCommand("errorCommand", null, new File("/"), 1000);
        Assert.assertEquals(CommandService.INVALID_COMMAND_EXIT_VALUE, executeInfo.getExitValue());
    }

    /**
     * CommandService#execCommand(java.lang.String, java.lang.String[], java.io.File, long)
     */
    @Test
    public void test_ExecCommand_by_one_command_when_command_is_valid_and_wait_enough_time() {
        CommandService commandService = new CommandService();
        ExecuteInfo executeInfo = commandService.execCommand("java -version", null, new File("/"), 10000);
        Assert.assertEquals(0, executeInfo.getExitValue());
    }

    /**
     * CommandService#execCommand(java.lang.String, java.lang.String[], java.io.File, long)
     */
    @Test
    public void test_ExecCommand_by_one_command_when_command_is_valid_and_always_wait() {
        CommandService commandService = new CommandService();
        ExecuteInfo executeInfo = commandService.execCommand("java -version", null, new File("/"), 0);
        Assert.assertEquals(0, executeInfo.getExitValue());
    }

    /**
     * CommandService#execCommand(java.lang.String, java.lang.String[], java.io.File, long)
     */
    @Test
    public void test_ExecCommand_by_one_command_when_command_is_valid_and_wait_1_millionSecond() {
        CommandService commandService = new CommandService();
        ExecuteInfo executeInfo = commandService.execCommand("java -version", null, new File("/"), 1);
        Assert.assertEquals(CommandService.INTERRUPTED_EXIT_VALUE, executeInfo.getExitValue());
    }

    /**
     * CommandService#execCommand(java.lang.String[], java.lang.String[], java.io.File, long)
     */
    @Test
    public void test_execCommand_by_array_command_when_command_is_empty() {
        CommandService commandService = new CommandService();
        ExecuteInfo executeInfo = commandService.execCommand(new String[]{}, null, new File("/"), 1000);
        Assert.assertEquals(CommandService.INVALID_PARAM_EXIT_VALUE, executeInfo.getExitValue());
    }

    /**
     * CommandService#execCommand(java.lang.String[], java.lang.String[], java.io.File, long)
     */
    @Test
    public void test_ExecCommand_by_array_command_when_command_throw_ioException() {
        CommandService commandService = new CommandService();
        ExecuteInfo executeInfo = commandService.execCommand(new String[]{"errorCommand"}, null, new File("/"), 1000);
        Assert.assertEquals(CommandService.INVALID_COMMAND_EXIT_VALUE, executeInfo.getExitValue());
    }

    /**
     * CommandService#execCommand(java.lang.String[], java.lang.String[], java.io.File, long)
     */
    @Test
    public void test_ExecCommand_by_array_command_when_command_is_valid_and_wait_enough_time() {
        CommandService commandService = new CommandService();
        ExecuteInfo executeInfo = commandService.execCommand(new String[]{"java", "-version"}, null, new File("/"), 10000);
        Assert.assertEquals(0, executeInfo.getExitValue());
    }

    /**
     * CommandService#execCommand(java.lang.String[], java.lang.String[], java.io.File, long)
     */
    @Test
    public void test_ExecCommand_by_array_command_when_command_is_valid_and_always_wait() {
        CommandService commandService = new CommandService();
        ExecuteInfo executeInfo = commandService.execCommand(new String[]{"java", "-version"}, null, new File("/"), 0);
        Assert.assertEquals(0, executeInfo.getExitValue());
    }

    /**
     * CommandService#execCommand(java.lang.String[], java.lang.String[], java.io.File, long)
     */
    @Test
    public void test_ExecCommand_by_array_command_when_command_is_valid_and_wait_1_millionSecond() {
        CommandService commandService = new CommandService();
        ExecuteInfo executeInfo = commandService.execCommand(new String[]{"java", "-version"}, null, new File("/"), 1);
        Assert.assertEquals(CommandService.INTERRUPTED_EXIT_VALUE, executeInfo.getExitValue());
    }

    /**
     * CommandService#execCommand(java.lang.String, long)
     */
    @Test
    public void test_execCommand_overloading1() {
        String command = "java -version";
        long waitTimeMillions = 0;
        CommandService commandService = PowerMockito.spy(new CommandService());
        ExecuteInfo executeInfo = commandService.execCommand(command, waitTimeMillions);
        try {
            PowerMockito.verifyPrivate(commandService)
                    .invoke("doExecute", command, new String[]{}, new File("."), waitTimeMillions);
        } catch (Exception e) {
            Assert.fail();
        }
        Assert.assertEquals(0, executeInfo.getExitValue());
    }

    /**
     * CommandService#execCommand(java.lang.String, java.lang.String[], long)
     */
    @Test
    public void test_execCommand_overloading2() {
        String command = "java -version";
        long waitTimeMillions = 0;
        CommandService commandService = PowerMockito.spy(new CommandService());
        ExecuteInfo executeInfo = commandService.execCommand(command, new String[]{"envs"}, waitTimeMillions);
        try {
            PowerMockito.verifyPrivate(commandService)
                    .invoke("doExecute", command, new String[]{}, new File("."), waitTimeMillions);
        } catch (Exception e) {
            Assert.fail();
        }
        Assert.assertEquals(0, executeInfo.getExitValue());
    }

    /**
     * CommandService#execCommand(java.lang.String[], long)
     */
    @Test
    public void test_execCommand_overloading3() {
        CommandService commandService = PowerMockito.spy(new CommandService());
        long waitTimeMillions = 0;
        String[] commands = {"java", "-version"};
        ExecuteInfo executeInfo = commandService.execCommand(commands, waitTimeMillions);
        try {
            PowerMockito.verifyPrivate(commandService)
                    .invoke("doExecute", commands, new String[]{}, new File("."), waitTimeMillions);
        } catch (Exception e) {
            Assert.fail();
        }
        Assert.assertEquals(0, executeInfo.getExitValue());
    }

    /**
     * CommandService#execCommand(java.lang.String[], java.lang.String[], long)
     */
    @Test
    public void test_execCommand_overloading4() {
        CommandService commandService = PowerMockito.spy(new CommandService());
        String[] commands = {"java", "-version"};
        String[] envProperties = {"envs"};
        long waitTimeMillions = 0;
        ExecuteInfo executeInfo = commandService.execCommand(commands, envProperties, waitTimeMillions);
        try {
            PowerMockito.verifyPrivate(commandService)
                    .invoke("doExecute", commands, envProperties, new File("."), waitTimeMillions);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
        Assert.assertEquals(0, executeInfo.getExitValue());
    }
}

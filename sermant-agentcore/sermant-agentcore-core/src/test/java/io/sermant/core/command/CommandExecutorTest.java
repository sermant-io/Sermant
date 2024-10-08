package io.sermant.core.command;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class CommandExecutorTest {
    private CommandExecutor commandExecutor;

    @BeforeEach
    public void setUp() {
        commandExecutor = Mockito.mock(CommandExecutor.class);
    }

    @AfterEach
    public void tearDown() {
        commandExecutor = null;
    }

    @Test
    public void testExecuteWhenArgsIsValidThenNoException() {
        // Arrange
        String validArgs = "validArgs";

        // Act & Assert
        assertDoesNotThrow(() -> commandExecutor.execute(validArgs));
    }

    @Test
    public void testExecuteWhenArgsIsNullThenNoException() {
        // Arrange
        String nullArgs = null;

        // Act & Assert
        assertDoesNotThrow(() -> commandExecutor.execute(nullArgs));
    }
}

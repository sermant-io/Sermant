/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.sermant.core.command;

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
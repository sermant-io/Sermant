/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.core.command;

/**
 * Enumeration of instructions for dynamic installation and uninstallation
 *
 * @author zhp
 * @since 2023-09-09
 */
public enum Command {
    /**
     * Uninstall agent instruction
     */
    UNINSTALL_AGENT("UNINSTALL-AGENT"),
    /**
     * Install plugin instruction
     */
    INSTALL_PLUGINS("INSTALL-PLUGINS"),
    /**
     * Uninstall plugin instruction
     */
    UNINSTALL_PLUGINS("UNINSTALL-PLUGINS"),

    /**
     * Enhancement query instruction
     */
    CHECK_ENHANCEMENT("CHECK-ENHANCEMENT");

    private final String value;

    Command(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

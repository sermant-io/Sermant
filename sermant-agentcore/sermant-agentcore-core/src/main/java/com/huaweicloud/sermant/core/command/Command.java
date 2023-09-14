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

package com.huaweicloud.sermant.core.command;

/**
 * 动态安装卸载的指令枚举
 *
 * @author zhp
 * @since 2023-09-09
 */
public enum Command {
    /**
     * 卸载agent指令
     */
    UNINSTALL_AGENT("UNINSTALL-AGENT"),
    /**
     * 安装插件指令
     */
    INSTALL_PLUGINS("INSTALL-PLUGINS"),
    /**
     * 卸载插件指令
     */
    UNINSTALL_PLUGINS("UNINSTALL-PLUGINS");
    private final String value;

    Command(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

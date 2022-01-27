/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.dubbo.register.constants;

/**
 * 常量
 *
 * @author provenceee
 * @date 2022/1/27
 */
public interface Constant {
    /**
     * sc注册协议名
     */
    String SC_REGISTRY_PROTOCOL = "sc";

    /**
     * 协议分隔符
     */
    String PROTOCOL_SEPARATION = "://";

    /**
     * sc初始化迁移规则
     */
    String SC_INIT_MIGRATION_RULE = "scInit";
}
/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.plugin.agent.declarer;

import net.bytebuddy.agent.builder.AgentBuilder;

/**
 * 插件描述，{@link PluginDeclarer}的低阶api
 * <p>用于描述插件的最终必要对象，由byte-buddy的{@link AgentBuilder.RawMatcher}和{@link AgentBuilder.Transformer}构成
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public interface PluginDescription extends AgentBuilder.RawMatcher, AgentBuilder.Transformer {
}

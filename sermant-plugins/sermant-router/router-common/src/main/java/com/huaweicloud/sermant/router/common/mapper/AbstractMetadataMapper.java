/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.common.mapper;

import java.util.Map;
import java.util.function.Function;

/**
 * metadata获取mapper
 *
 * @param <I> 实例泛型
 * @author chengyouling
 * @since 2024-03-13
 */
public abstract class AbstractMetadataMapper<I> implements Function<I, Map<String, String>> {
}
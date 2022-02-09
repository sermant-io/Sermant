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
package com.huawei.sermant.core.service.dynamicconfig.api;

import java.util.List;

import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

/**
 * 对同组下所有键做操作
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-12-14
 */
public interface GroupService {
    /**
     * 获取组中所有键
     *
     * @param group 组名
     * @return 键集合
     */
    List<String> listKeysFromGroup(String group);

    /**
     * 为组下所有的键添加监听器
     *
     * @param group    组名
     * @param listener 监听器
     * @return 是否添加成功
     */
    boolean addGroupListener(String group, DynamicConfigListener listener);

    /**
     * 移除组下所有键的监听器
     *
     * @param group 组名
     * @return 是否全部移除成功
     */
    boolean removeGroupListener(String group);
}

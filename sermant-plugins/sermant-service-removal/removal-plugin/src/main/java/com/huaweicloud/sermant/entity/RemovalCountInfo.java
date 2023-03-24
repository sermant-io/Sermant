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

package com.huaweicloud.sermant.entity;

/**
 * 移除的离群实例统计信息
 *
 * @author zhp
 * @since 2023-02-28
 */
public class RemovalCountInfo {
    /**
     * 被摘除数量
     */
    private int removalCount;

    /**
     * 剩余数量
     */
    private int remainderCount;

    public int getRemovalCount() {
        return removalCount;
    }

    public void setRemovalCount(int removalCount) {
        this.removalCount = removalCount;
    }

    public int getRemainderCount() {
        return remainderCount;
    }

    public void setRemainderCount(int remainderCount) {
        this.remainderCount = remainderCount;
    }
}

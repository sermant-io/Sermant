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

package com.huawei.sermant.backend.entity;

/**
 * 飞书支持的支持的数据类型
 *
 * @since 2023-03-02
 * @author xuezechao
 */
public enum FeiShuMessageType {

    /**
     * 文本
     */
    TEXT {
        public String toString() {
            return "text";
        }
    },

    /**
     * 富文本
     */
    POST {
        public String toString() {
            return "post";
        }
    },

    /**
     * 消息卡片
     */
    INTERACTIVE {
        public String toString() {
            return "interactive";
        }
    },
}

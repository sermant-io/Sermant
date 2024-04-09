/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.backend.entity.event;

import lombok.Getter;
import lombok.Setter;

/**
 * FeiShu Rich Content Entity
 *
 * @author xuezechao
 * @since 2023-03-02
 */
@Getter
@Setter
public class FeiShuContentEntity {
    private String tag;

    private String text;

    /**
     * Constructor
     *
     * @param tag tag
     * @param text content
     */
    public FeiShuContentEntity(String tag, String text) {
        this.tag = tag;
        this.text = text;
    }
}

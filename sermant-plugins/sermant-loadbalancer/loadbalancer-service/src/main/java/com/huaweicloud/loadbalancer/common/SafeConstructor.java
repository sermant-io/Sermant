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

package com.huaweicloud.loadbalancer.common;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.List;
import java.util.Map;

/**
 * YAML安全构造器
 *
 * @author zhp
 * @since 2022-10-25
 */
public class SafeConstructor extends Constructor {
    /**
     * 构造器
     *
     * @param whiteList 白名单
     */
    public SafeConstructor(List<String> whiteList) {
        super();
        this.yamlConstructors.put(null, undefinedConstructor);
        this.yamlConstructors.put(new Tag(Tag.PREFIX + Map.class.getCanonicalName()), new SafeConstructObject());
        this.yamlConstructors.put(new Tag(Tag.PREFIX + List.class.getCanonicalName()), new SafeConstructObject());
        addWhiteList(whiteList);
    }

    /**
     * 增加白名单
     *
     * @param whiteList 白名单
     */
    private void addWhiteList(List<String> whiteList) {
        if (whiteList == null || whiteList.size() == 0) {
            return;
        }
        for (String whiteName : whiteList) {
            super.yamlConstructors.put(new Tag(Tag.PREFIX + whiteName), new SafeConstructObject());
        }
    }

    /**
     * 构造Yaml对象
     *
     * @author zhp
     * @since 2022-10-26
     */
    protected class SafeConstructObject extends ConstructYamlObject {
        SafeConstructObject() {
            super();
        }
    }
}

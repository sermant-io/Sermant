/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.util;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * 继承自{@link Constructor}类，用于添加对Yaml操作的白名单，解决安全问题
 *
 * @author Zhang Hu
 * @since 2021-05-25
 */
public class SafeYamlConstructor extends Constructor {
    public SafeYamlConstructor() {
        super();
        List<String> whiteList = new ArrayList<>();
        whiteList.add("java.util.Map");

        // 除白名单的所有类都报错
        this.yamlConstructors.put(null, undefinedConstructor);

        // 为白名单的类添加解析类
        addWhiteList(whiteList);
    }

    private void addWhiteList(List<String> whiteList) {
        if (whiteList == null) {
            return;
        }
        whiteList.forEach(className -> this.yamlConstructors.put(new Tag(Tag.PREFIX + className), new SafeYamlConstructorObject()));
    }

    /**
     * 继承自{@link ConstructYamlObject}类，用于解析目标类
     *
     * @author Zhang Hu
     * @since 2021-05-25
     */
    protected class SafeYamlConstructorObject extends ConstructYamlObject {
        public SafeYamlConstructorObject() {
            super();
        }
    }
}

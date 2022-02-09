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

package com.huawei.sermant.core.plugin.adaptor.remapper;

import com.huawei.sermant.core.plugin.adaptor.config.AdaptorSetting;

import org.objectweb.asm.commons.Remapper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义Remapper，用于修正全限定名和路径
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-18
 */
public class AdaptorRemapper extends Remapper {
    /**
     * 类的匹配格式
     */
    private final Pattern classPattern = Pattern.compile("(\\[*)?L(.+);");

    /**
     * 全限定名修正的mapping集
     */
    private final List<AdaptorSetting.ShadeMappingInfo> shadeMappings;

    public AdaptorRemapper(List<AdaptorSetting.ShadeMappingInfo> shadeMappings) {
        this.shadeMappings = shadeMappings;
    }

    /**
     * 修正字符串型字段，需要修正全限定名和路径名
     *
     * @param value 字段值
     * @return 修正后结果
     */
    @Override
    public Object mapValue(Object value) {
        if (value instanceof String) {
            String prefix = "";
            String suffix = "";
            String fieldValStr = (String) value;
            final Matcher matcher = classPattern.matcher((String) value);
            if (matcher.matches()) {
                prefix = matcher.group(1) + "L";
                suffix = ";";
                fieldValStr = matcher.group(2);
            }
            for (AdaptorSetting.ShadeMappingInfo shadeMapping : shadeMappings) {
                if (fieldValStr.startsWith(shadeMapping.getSourcePattern())) {
                    return prefix + shadeMapping.relocateClass(fieldValStr) + suffix;
                } else if (fieldValStr.startsWith(shadeMapping.getSourcePathPattern())) {
                    return prefix + shadeMapping.relocatePath(fieldValStr) + suffix;
                }
            }
            return value;
        }
        return super.mapValue(value);
    }

    /**
     * 修正类型
     *
     * @param internalName 类型名称
     * @return 修正后的类型
     */
    @Override
    public String map(String internalName) {
        String prefix = "";
        String suffix = "";
        String jarStr = internalName;
        final Matcher matcher = classPattern.matcher(internalName);
        if (matcher.matches()) {
            prefix = matcher.group(1) + "L";
            suffix = ";";
            jarStr = matcher.group(2);
        }
        for (AdaptorSetting.ShadeMappingInfo shadeMapping : shadeMappings) {
            if (jarStr.startsWith(shadeMapping.getSourcePathPattern())) {
                return prefix + shadeMapping.relocatePath(jarStr) + suffix;
            }
        }
        return internalName;
    }

    /**
     * 修正spi配置文件
     *
     * @param line spi文件的一行内容
     * @return 修正后的一行内容
     */
    public String mapServiceResource(String line) {
        for (AdaptorSetting.ShadeMappingInfo shadeMapping : shadeMappings) {
            if (line.startsWith(shadeMapping.getSourcePattern())) {
                return shadeMapping.relocateClass(line);
            }
        }
        return line;
    }
}

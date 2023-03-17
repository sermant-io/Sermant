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

package com.huaweicloud.sermant.router.common.utils;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.config.RouterConfig;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 流量标识解码工具类.
 *
 * @author yangrh
 * @since 2022-10-25
 */
public class FlowContextUtils {
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    private static final int TAG_PART_NUM = 2;

    private static volatile RouterConfig routerConfig;

    private FlowContextUtils() {
    }

    /**
     * 解析base64加密信息成明文信息
     *
     * @param encodeTagsString encodeTagsString
     * @return 解密后的流量标识
     */
    public static Map<String, List<String>> decodeTags(String encodeTagsString) {
        if (StringUtils.isEmpty(encodeTagsString)) {
            return Collections.emptyMap();
        }
        String[] tags = encodeTagsString.split(",");
        Map<String, List<String>> tagMapping = new HashMap<>();
        for (String tag : tags) {
            final String[] parts = tag.split(":");
            if (parts.length != TAG_PART_NUM) {
                continue;
            }
            List<String> list = new ArrayList<>();
            list.add(new String(DECODER.decode(parts[1]), StandardCharsets.UTF_8));
            tagMapping.put(decode(parts[0]).toLowerCase(Locale.ENGLISH), list);
        }
        return tagMapping;
    }

    /**
     * 解码附件
     *
     * @param attachments 附件信息
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public static Map<String, Object> decodeAttachments(Map<String, Object> attachments) {
        if (attachments == null || attachments.isEmpty() || StringUtils.isBlank(getTagName())) {
            return attachments;
        }
        Object encode = attachments.get(getTagName());
        if (encode == null) {
            return attachments;
        }
        String encodeTags = String.valueOf(encode);
        Map<String, Object> allAttachments = new HashMap<>(attachments);
        String[] tags = encodeTags.split(",");
        for (String tag : tags) {
            final String[] parts = tag.split(":");
            if (parts.length != TAG_PART_NUM) {
                continue;
            }
            allAttachments.put(decode(parts[0]).toLowerCase(Locale.ENGLISH), decode(parts[1]));
        }
        return Collections.unmodifiableMap(allAttachments);
    }

    /**
     * 解码
     *
     * @param encodeString 编码的字符串
     * @return {@link String}
     */
    private static String decode(String encodeString) {
        return new String(DECODER.decode(encodeString), StandardCharsets.UTF_8);
    }

    /**
     * 获取请求头中需要解析的tag
     *
     * @return {@link String}
     */
    public static String getTagName() {
        if (routerConfig == null) {
            synchronized (FlowContextUtils.class) {
                if (routerConfig == null) {
                    routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
                    return routerConfig.getParseHeaderTag();
                }
            }
        }
        return routerConfig.getParseHeaderTag();
    }
}

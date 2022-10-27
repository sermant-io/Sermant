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

import com.huaweicloud.sermant.core.utils.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    private FlowContextUtils() {
    }

    /**
     * sw8-correlation流量标识解码.
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
            tagMapping.put(new String(DECODER.decode(parts[0]), StandardCharsets.UTF_8), list);
        }
        return tagMapping;
    }
}

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

package com.huawei.sermant.core.lubanops.integration.utils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.ValueFilter;

import java.util.List;

public class JSON {

    public static final String SECURITY_CODE = "******";

    public static String toJSONString(Object obj) {
        String jsonString = JSONObject.toJSONString(obj);
        return jsonString;
    }

    public static String toJSONString(Object obj, List<String> excludeKeys) {
        String jsonString = JSONObject.toJSONString(obj, new SecurityPropertyFilter(excludeKeys));
        return jsonString;
    }

    public static <T> T parseObject(String text, Class<T> type) {
        T obj = JSONObject.parseObject(text, type);
        return obj;
    }

    public static <T> T parseObject(String text, TypeReference<T> type) {
        T obj = JSONObject.parseObject(text, type);
        return obj;
    }

    public static <T> T parseObject(byte[] text, Class<T> type) {
        T obj = JSONObject.parseObject(text, type);
        return obj;
    }

    /**
     * <br>
     * @param s
     * @param type
     * @return
     * @author
     * @since 2019年11月6日
     */
    public static <T> List<T> parseList(String s, Class<T> type) {
        List<T> list = JSONObject.parseArray(s, type);
        return list;
    }

    public static byte[] toJSONBytes(Object object) {
        byte[] jsonByte = JSONObject.toJSONBytes(object);
        return jsonByte;
    }

    public static int[] parseIntArray(String s) {
        List<Integer> list = JSON.parseList(s, Integer.class);
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i).intValue();
        }
        return array;
    }

    static class SecurityPropertyFilter implements ValueFilter {
        List<String> excludeKeys;

        public SecurityPropertyFilter(List<String> excludeKeys) {
            this.excludeKeys = excludeKeys;
        }

        @Override
        public Object process(Object source, String name, Object value) {

            if (excludeKeys.contains(name)) {
                return SECURITY_CODE;
            }
            return source;
        }
    }

}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.argus.common;

import com.google.gson.*;

import java.util.*;

/**
 * json 工具类
 * @author xushiheng
 */
public  class JsonUtil {

	private final static Gson INSTANCE = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss")
		.create();

	private JsonUtil(){}

	public static Gson getInstance(){
		return INSTANCE;
	}
    /**
     * @return JsonObject
     */
    public static JsonObject getJsonObjectFromClient(String json) throws Exception {
        if (json == null || "".equals(json)) {
            throw new Exception();
        }
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(json);
        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        throw new Exception();
    }

    /**
     * @throws Exception
     */
    public static String get(String json,String key) throws Exception {
        JsonPrimitive o = (JsonPrimitive) getJsonObjectFromClient(json).get(key);
        if(o != null)return  o.getAsString();
        return "";
    }

    /**
     * @throws Exception
     */
    public static JsonObject getJsonObject(String json,String key) throws Exception {
        return getJsonObjectFromClient(json).getAsJsonObject(key);
    }
    /**
     * json 字符串转json对象
     *
     * @param json
     * @return
     */
    public static JsonObject parseJson(String json) {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(json).getAsJsonObject();
        return obj;
    }

    /**
     * 根据json字符串返回Map对象
     *
     * @param json
     * @return
     */
    public static Map<String, Object> toMap(String json) {
        return JsonUtil.toMap(JsonUtil.parseJson(json));
    }

    public static Map<String, Object> toMapString(String json) {
        return JsonUtil.toMapString(JsonUtil.parseJson(json));
    }

    public static Map<String, Object> toMapString(JsonObject json) {
        Map<String, Object> map = new HashMap<String, Object>();
        Set<Map.Entry<String, JsonElement>> entrySet = json.entrySet();
        for (Iterator<Map.Entry<String, JsonElement>> iter = entrySet.iterator(); iter.hasNext(); ) {
            Map.Entry<String, JsonElement> entry = iter.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof JsonArray)
                map.put((String) key, toList((JsonArray) value));
            else if (value instanceof JsonObject)
                map.put((String) key, toMapString((JsonObject) value));
            else
                map.put((String) key, ((JsonPrimitive)value).getAsString());

        }
        return map;
    }

    /**
     * 将JSONObjec对象转换成Map-List集合
     * @param json
     * @return
     */
    public static Map<String, Object> toMap(JsonObject json) {
        Map<String, Object> map = new HashMap<String, Object>();
        Set<Map.Entry<String, JsonElement>> entrySet = json.entrySet();
        for (Iterator<Map.Entry<String, JsonElement>> iter = entrySet.iterator(); iter.hasNext(); ) {
            Map.Entry<String, JsonElement> entry = iter.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof JsonArray)
                map.put((String) key, toList((JsonArray) value));
            else if (value instanceof JsonObject)
                map.put((String) key, toMap((JsonObject) value));
            else if(value instanceof  JsonPrimitive){
                JsonPrimitive pri = (JsonPrimitive)value;
                if(pri.isBoolean()){
                    map.put((String) key, pri.getAsBoolean());
                };
                if(pri.isNumber()){
                    map.put((String) key, pri.getAsNumber());
                };
                if(pri.isString()){
                    map.put((String) key, pri.getAsString());
                };
            }
            else
                map.put((String) key, value);
        }
        return map;
    }

    /**
     * 将JSONArray对象转换成List集合
     * @param json
     * @return
     */
    public static List<Object> toList(JsonArray json) {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < json.size(); i++) {
            Object value = json.get(i);
            if (value instanceof JsonArray) {
                list.add(toList((JsonArray) value));
            } else if (value instanceof JsonObject) {
                list.add(toMap((JsonObject) value));
            } else {
                list.add(value);
            }
        }
        return list;
    }

}

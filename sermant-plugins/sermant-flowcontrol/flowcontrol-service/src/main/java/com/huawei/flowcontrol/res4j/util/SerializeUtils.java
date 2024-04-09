/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.res4j.util;

import com.huawei.flowcontrol.common.config.CommonConst;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * serialization tool
 *
 * @author zhouss
 * @since 2022-08-08
 */
public class SerializeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final ObjectMapper MAPPER = new MessageMapper();

    private SerializeUtils() {
    }

    /**
     * serialize objects to strings
     *
     * @param obj target object
     * @return string
     */
    public static Optional<String> serialize2String(Object obj) {
        if (obj == null) {
            return Optional.of(CommonConst.EMPTY_STR);
        }
        try {
            return Optional.of(MAPPER.writeValueAsString(obj));
        } catch (JsonProcessingException ex) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                    "Can not serialize class [%s] to string", obj.getClass().getName()));
        }
        return Optional.of(CommonConst.EMPTY_STR);
    }

    /**
     * Message mapper, mainly do some configuration presets
     *
     * @since 2022-08-08
     */
    static class MessageMapper extends ObjectMapper {
        MessageMapper() {
            this.getFactory().disable(Feature.AUTO_CLOSE_SOURCE);
            this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            this.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            this._deserializationConfig = (DeserializationConfig) this._deserializationConfig
                    .without(new MapperFeature[]{MapperFeature.DEFAULT_VIEW_INCLUSION});
            this._serializationConfig = (SerializationConfig) this._serializationConfig
                    .without(new MapperFeature[]{MapperFeature.DEFAULT_VIEW_INCLUSION});
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            this.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
            this.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        }
    }
}

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

package io.sermant.implement.service.dynamicconfig.kie.client.kie;

import com.alibaba.fastjson.JSONObject;

import io.sermant.implement.service.dynamicconfig.kie.client.http.HttpResult;

import org.apache.http.HttpStatus;

import java.util.Iterator;
import java.util.List;

/**
 * Kie result handler
 *
 * @param <R> handle泛型
 * @author zhouss
 * @since 2021-11-17
 */
public interface ResultHandler<R> {
    /**
     * process result
     *
     * @param result HttpResult
     * @return R
     */
    R handle(HttpResult result);

    /**
     * Default result handler
     *
     * @author zhouss
     * @since 2021-11-17
     */
    class DefaultResultHandler implements ResultHandler<KieResponse> {
        private final boolean onlyEnabled;

        /**
         * Constructor
         */
        public DefaultResultHandler() {
            onlyEnabled = true;
        }

        /**
         * Constructor
         *
         * @param onlyEnabled Identification: Whether to only obtain the configuration for startup
         */
        public DefaultResultHandler(boolean onlyEnabled) {
            this.onlyEnabled = onlyEnabled;
        }

        @Override
        public KieResponse handle(HttpResult result) {
            if (result.isError()) {
                return null;
            }
            final String content = result.getResult();
            final KieResponse kieResponse = JSONObject.parseObject(content, KieResponse.class);
            if (kieResponse == null) {
                return null;
            }
            if (result.getCode() == HttpStatus.SC_NOT_MODIFIED) {
                // If the response status code is 304, there is no change for related key
                kieResponse.setChanged(false);
            }

            // Filter out the disabled kv configuration
            final List<KieConfigEntity> data = kieResponse.getData();
            if (data == null) {
                return kieResponse;
            }
            kieResponse.setRevision(String.valueOf(result.getResponseHeaders().get("X-Kie-Revision")));
            filter(data);
            kieResponse.setTotal(data.size());
            return kieResponse;
        }

        /**
         * Filter kv that is not turned on
         *
         * @param data kv list
         */
        private void filter(List<KieConfigEntity> data) {
            if (!onlyEnabled) {
                return;
            }
            data.removeIf(next -> "disabled".equals(next.getStatus()));
        }
    }
}

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

package com.huaweicloud.sermant.core.service.dynamicconfig.kie.client.kie;

import com.huaweicloud.sermant.core.service.dynamicconfig.kie.client.http.HttpResult;

import com.alibaba.fastjson.JSONObject;

import org.apache.http.HttpStatus;

import java.util.Iterator;
import java.util.List;

/**
 * Kie结果处理器
 *
 * @author zhouss
 * @since 2021-11-17
 */
public interface ResultHandler<R> {
    /**
     * 处理响应结果
     *
     * @param result 响应
     * @return R
     */
    R handle(HttpResult result);

    /**
     * 默认结果处理器
     */
    class DefaultResultHandler implements ResultHandler<KieResponse> {
        private final boolean onlyEnabled;
        public DefaultResultHandler() {
            onlyEnabled = true;
        }

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
                // KIE如果响应状态码为304，则表示没有相关键变更
                kieResponse.setChanged(false);
            }
            // 过滤掉disabled的kv配置
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
         * 过滤未开启的kv
         *
         * @param data kv列表
         */
        private void filter(List<KieConfigEntity> data) {
            if (!onlyEnabled) {
                return;
            }
            final Iterator<KieConfigEntity> iterator = data.iterator();
            while (iterator.hasNext()) {
                final KieConfigEntity next = iterator.next();
                if ("disabled".equals(next.getStatus())) {
                    iterator.remove();
                }
            }
        }
    }
}

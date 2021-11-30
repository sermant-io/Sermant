/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.service.dynamicconfig.kie.client.kie;

import com.alibaba.fastjson.JSONObject;
import com.huawei.apm.core.service.dynamicconfig.kie.client.http.HttpResult;
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

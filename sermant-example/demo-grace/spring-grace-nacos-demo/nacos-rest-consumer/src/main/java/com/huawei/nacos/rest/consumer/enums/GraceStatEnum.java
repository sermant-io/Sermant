/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.nacos.rest.consumer.enums;

import com.huawei.nacos.rest.consumer.stat.DefaultRequestStat;
import com.huawei.nacos.rest.consumer.stat.HotRequestStat;
import com.huawei.nacos.rest.consumer.stat.RequestStat;

import java.util.Optional;

/**
 * 统计枚举
 *
 * @author zhouss
 * @since 2022-06-17
 */
public enum GraceStatEnum {
    /**
     * 开启优雅下线
     */
    GRACE_DOWN_OPEN("graceDownOpen", "开启优雅下线", new DefaultRequestStat("graceDownOpen")),

    /**
     * 关闭优雅下线
     */
    GRACE_DOWN_CLOSE("graceDownClose", "关闭优雅下线", new DefaultRequestStat("graceDownClose")),

    /**
     * 服务预热测试
     */
    GRACE_HOT("graceHot", "服务预热", new HotRequestStat("graceHot"));

    private String url;

    private String desc;

    private RequestStat requestStat;

    GraceStatEnum(String url, String desc, RequestStat requestStat) {
        this.url = url;
        this.desc = desc;
        this.requestStat = requestStat;
    }

    /**
     * 匹配统计枚举
     *
     * @param url 请求接口
     * @return GraceStatEnum
     */
    public static Optional<GraceStatEnum> match(String url) {
        for (GraceStatEnum graceStatEnum : values()) {
            if (graceStatEnum.getUrl().equals(url)) {
                return Optional.of(graceStatEnum);
            }
        }
        return Optional.empty();
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public RequestStat getRequestStat() {
        return requestStat;
    }

    public void setRequestStat(RequestStat requestStat) {
        this.requestStat = requestStat;
    }
}

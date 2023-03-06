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

package com.huaweicloud.sermant.backend.entity;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeartBeatResult extends Result {
    /**
     * 下一次心跳的周期
     */
    private Integer heartBeatInterval;

    /**
     * 附属信息，收到这个信息后，原封不动得通过数据上报
     */
    private Map<String, String> attachment;

    private List<MonitorItem> monitorItemList;

    /**
     * 系统属性
     */
    private Map<String, String> systemProperties;

    /**
     * access的地址描述
     */
    private List<Address> accessAddressList;

    /**
     * 实例状态0 代表ok， 1代表disabled
     */
    private Integer instanceStatus;

    /**
     * 对结果进行md5计算，如果内容变化了就下发新的配置
     */
    private String md5;
}

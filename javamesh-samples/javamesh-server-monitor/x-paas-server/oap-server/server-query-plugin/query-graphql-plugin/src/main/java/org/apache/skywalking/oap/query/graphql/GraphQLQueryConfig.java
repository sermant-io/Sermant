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

package org.apache.skywalking.oap.query.graphql;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.skywalking.oap.server.library.module.ModuleConfig;

/**
 * The config of {@code query.graphql}.
 */
//@Getter(AccessLevel.PACKAGE)  update huawei APM issue#46 change log:druid连接池信息采集,新增类需要在其他包调用
@Getter(AccessLevel.PUBLIC)
@Setter
public class GraphQLQueryConfig extends ModuleConfig {
    private String path;

    // update huawei APM issue#46, change log:druid连接池信息采集,增加查询存活状态判定的有效区间
    /**
     * 判定应用，实例，数据源是否存活的时间区间
     * 如果记录在前aliveTime分钟有更新，则判定当前是存活状态
     * 单位：分钟
     */
    private int aliveTime;
}

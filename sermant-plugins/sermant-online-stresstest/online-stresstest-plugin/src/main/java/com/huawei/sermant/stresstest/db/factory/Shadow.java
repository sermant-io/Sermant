/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.stresstest.db.factory;

import com.huawei.sermant.stresstest.config.bean.DataSourceInfo;

import javax.sql.DataSource;

/**
 * Shadow 数据库接口类
 *
 * @author yiwei
 * @since 2021-10-21
 */
public interface Shadow {
    /**
     * 生成影子的datasource
     *
     * @param source 原始的datasource
     * @param shadowInfo 影子库信息
     * @return 影子datasource
     */
    DataSource shadowDataSource(DataSource source, DataSourceInfo shadowInfo);
}

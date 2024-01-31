/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.metrics.util;

import com.huaweicloud.sermant.core.utils.StringUtils;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.util.TablesNamesFinder;

/**
 * 表名获取工具类
 *
 * @author zhp
 * @since 2024-01-15
 */
public class MysqlTablesNameFinder extends TablesNamesFinder {
    private static final String INDEX_TYPE = "INDEX";

    @Override
    public void visit(Drop drop) {
        Table table = drop.getName();
        if (StringUtils.equals(drop.getType(), INDEX_TYPE) || table == null
                || StringUtils.isEmpty(table.getName())) {
            return;
        }
        visit(drop.getName());
    }

    @Override
    public void visit(CreateIndex createIndex) {
        visit(createIndex.getTable());
    }

    @Override
    public void visit(Alter alter) {
        visit(alter.getTable());
    }
}

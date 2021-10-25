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

package org.apache.skywalking.oap.server.core.alarm;

import com.huawei.apm.core.drill.BaseReplicator;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public abstract class MetaInAlarm {
    @Getter
    @Setter
    private int copy;// huawei update.无损演练新增属性

    public abstract String getScope();

    public abstract int getScopeId();

    public abstract String getName();

    public abstract String getMetricsName();

    /**
     * In most scopes, there is only id0, as primary id. Such as Service, Endpoint. But in relation, the ID includes
     * two, actually. Such as ServiceRelation, id0 represents the sourceScopeId service id
     *
     * @return the primary id.
     */
    public abstract String getId0();

    /**
     * Only exist in multiple IDs case, Such as ServiceRelation, id1 represents the dest service id
     */
    public abstract String getId1();

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MetaInAlarm that = (MetaInAlarm) o;
        // huawei update.无损演练：添加复制标签
        if (this.copy != that.getCopy()) {
            return false;
        }
        return getId0().equals(that.getId0()) && getId1().equals(that.getId1());
    }

    @Override
    public int hashCode() {
        // huawei update.无损演练：添加复制标签
        if (this.copy == BaseReplicator.TO_COPY) {
            return Objects.hash(getId0(), getId1()) + BaseReplicator.COPY_FLAG.hashCode();
        }
        return Objects.hash(getId0(), getId1());
    }
}

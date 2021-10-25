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

package org.apache.skywalking.oap.server.storage.plugin.elasticsearch7.query;

import java.io.IOException;

import com.huawei.apm.core.drill.BaseReplicator;
import com.huawei.apm.core.drill.DrillThreadLocal;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.core.query.input.Duration;
import org.apache.skywalking.oap.server.core.query.input.MetricsCondition;
import org.apache.skywalking.oap.server.core.query.sql.Function;
import org.apache.skywalking.oap.server.core.storage.annotation.ValueColumnMetadata;
import org.apache.skywalking.oap.server.library.client.elasticsearch.ElasticSearchClient;
import org.apache.skywalking.oap.server.storage.plugin.elasticsearch.query.MetricsQueryEsDAO;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.builder.SearchSourceBuilder;

/**
 * Duplicate logic of {@link MetricsQueryEsDAO}, but for making compatible in ElasticSearch 7.
 *
 * {@link Avg} and {@link Sum} have different package in the es7.
 */
public class MetricsQueryEs7DAO extends MetricsQueryEsDAO {

    public MetricsQueryEs7DAO(ElasticSearchClient client) {
        super(client);
    }

    @Override
    public int readMetricsValue(final MetricsCondition condition,
                                final String valueColumnName,
                                final Duration duration) throws IOException {
        SearchSourceBuilder sourceBuilder = SearchSourceBuilder.searchSource();
        buildQuery(sourceBuilder, condition, duration);

        TermsAggregationBuilder entityIdAggregation = AggregationBuilders.terms(Metrics.ENTITY_ID)
                                                                         .field(Metrics.ENTITY_ID)
                                                                         .size(1);
        final Function function = ValueColumnMetadata.INSTANCE.getValueFunction(condition.getName());
        functionAggregation(function, entityIdAggregation, valueColumnName);

        sourceBuilder.aggregation(entityIdAggregation);

        String indexName = condition.getName();
        // huawei update.无损演练：查询新增flag值
        if (DrillThreadLocal.isDrillFlag()) {
            indexName = indexName + BaseReplicator.COPY_FLAG;
        }
        SearchResponse response = getClient().search(indexName, sourceBuilder);

        Terms idTerms = response.getAggregations().get(Metrics.ENTITY_ID);
        for (Terms.Bucket idBucket : idTerms.getBuckets()) {
            switch (function) {
                case Sum:
                    Sum sum = idBucket.getAggregations().get(valueColumnName);
                    return (int) sum.getValue();
                case Avg:
                    Avg avg = idBucket.getAggregations().get(valueColumnName);
                    return (int) avg.getValue();
                default:
                    avg = idBucket.getAggregations().get(valueColumnName);
                    return (int) avg.getValue();
            }
        }
        return ValueColumnMetadata.INSTANCE.getDefaultValue(condition.getName());
    }

    protected void functionAggregation(Function function, TermsAggregationBuilder parentAggBuilder, String valueCName) {
        switch (function) {
            case Avg:
                parentAggBuilder.subAggregation(AggregationBuilders.avg(valueCName).field(valueCName));
                break;
            case Sum:
                parentAggBuilder.subAggregation(AggregationBuilders.sum(valueCName).field(valueCName));
                break;
            default:
                parentAggBuilder.subAggregation(AggregationBuilders.avg(valueCName).field(valueCName));
                break;
        }
    }
}

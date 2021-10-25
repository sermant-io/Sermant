/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.oap.server.storage.plugin.elasticsearch7;

import com.huawei.apm.core.drill.BaseReplicator;
import com.huawei.apm.core.drill.DrillThreadLocal;
import com.huawei.apm.core.query.NodeCondition;
import com.huawei.apm.core.query.NodeRecord;
import com.huawei.apm.core.query.NodeRecords;
import com.huawei.apm.core.query.dao.IDruidQueryDao;
import com.huawei.apm.core.source.ConnectionPoolMetric;

import org.apache.skywalking.apm.util.StringUtil;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;
import org.apache.skywalking.oap.server.storage.plugin.elasticsearch7.Es7DAO;
import org.apache.skywalking.oap.server.storage.plugin.elasticsearch7.client.ElasticSearch7Client;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.aggregations.pipeline.BucketSortPipelineAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * druid信息查询，es实现
 *
 * @author zhouss
 * @since 2020-11-29
 */
public class DruidQueryEs7DAO extends Es7DAO implements IDruidQueryDao {
    /**
     * 分组名称
     */
    private final String groupName = "group";

    /**
     * 分组，统计总记录数
     */
    private final String groupTotal = "group_total";

    /**
     * 每组最新记录分组名称
     */
    private final String topName = "latest_record";

    /**
     * 聚合分页 分片名称
     */
    private final String aggregationPageBucket = "page_bucket";

    /**
     * 用于模糊查询
     */
    private final String fuzzyMark = "*";

    /**
     * 分组每组的最新一条记录
     */
    private final int topOne = 1;

    /**
     * es返回的最大分组记录数
     */
    private final int returnMaxSize = 10000;

    /**
     * 指定需查询的字段
     */
    private final String[] fetchSourceFields = {
        ConnectionPoolMetric.ENTITY_ID,
        ConnectionPoolMetric.SERVICE_ID,
        ConnectionPoolMetric.ACTIVE_COUNT,
        ConnectionPoolMetric.MAX_ACTIVE,
        ConnectionPoolMetric.NAME,
        ConnectionPoolMetric.PARENT_NAME,
        ConnectionPoolMetric.POOLING_COUNT,
        ConnectionPoolMetric.DATABASE_PEERS
    };

    public DruidQueryEs7DAO(ElasticSearch7Client client) {
        super(client);
    }

    @Override
    public NodeRecords queryNodeRecords(NodeCondition condition) throws IOException {
        SearchSourceBuilder sourceBuilder = SearchSourceBuilder.searchSource();

        // 构建基本查询条件
        BoolQueryBuilder boolQueryBuilder = this.buildBaseQueryBuilder(condition);
        sourceBuilder.query(boolQueryBuilder).sort(Metrics.TIME_BUCKET, SortOrder.DESC);
        sourceBuilder.aggregation(this.buildTermsAggregation(condition.getGroupByColumn(), groupTotal));

        // 分页处理
        if (condition.getPageNum() != null && condition.getPageSize() != null
                && condition.getPageNum() > 0 && condition.getPageSize() > 0) {
            sourceBuilder.aggregation(this.buildPageTermsAggregationBuilder(condition, groupName));
        }

        String indexName = condition.getMetricName();
        // huawei update.无损演练：查询新增flag值
        if (DrillThreadLocal.isDrillFlag()) {
            indexName = indexName + BaseReplicator.COPY_FLAG;
        }

        SearchResponse response = getClient().search(indexName, sourceBuilder);
        Terms terms = response.getAggregations().get(groupName);
        Terms termsTotal = response.getAggregations().get(groupTotal);

        // 如果没有进行分页处理，则采用所有的分组
        terms = terms == null ? termsTotal : terms;
        NodeRecords nodeRecords = packageRecords(terms);
        nodeRecords.setTotal(termsTotal.getBuckets().size());
        return nodeRecords;
    }

    /**
     * 打包分组记录
     *
     * @param terms 聚合分组记录
     * @return 记录
     */
    private NodeRecords packageRecords(Terms terms) {
        NodeRecords nodeRecords = new NodeRecords();
        if (CollectionUtils.isEmpty(terms.getBuckets())) {
            return nodeRecords;
        }
        terms.getBuckets().forEach(bucket -> {
            TopHits hits = bucket.getAggregations().get(topName);
            if (hits == null || hits.getHits().getTotalHits().value == 0) {
                return;
            }
            for (SearchHit hit : hits.getHits()) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                nodeRecords.getRecords().add(NodeRecord.builder()
                    .serviceId(String.valueOf(sourceAsMap.get(ConnectionPoolMetric.SERVICE_ID)))
                    .entityId(String.valueOf(sourceAsMap.get(ConnectionPoolMetric.ENTITY_ID)))
                    .poolingCount(Long.parseLong(String.valueOf(
                            sourceAsMap.getOrDefault(ConnectionPoolMetric.POOLING_COUNT, 0L))))
                    .maxActive(Long.parseLong(String.valueOf(
                            sourceAsMap.getOrDefault(ConnectionPoolMetric.MAX_ACTIVE, 0L))))
                    .activeCount(Long.parseLong(String.valueOf(
                            sourceAsMap.getOrDefault(ConnectionPoolMetric.ACTIVE_COUNT, 0L))))
                    .name(String.valueOf(sourceAsMap.get(ConnectionPoolMetric.NAME)))
                    .parentName(String.valueOf(sourceAsMap.get(ConnectionPoolMetric.PARENT_NAME)))
                    .databasePeers(String.valueOf(sourceAsMap.get(ConnectionPoolMetric.DATABASE_PEERS)))
                    .build());
            }
        });
        return nodeRecords;
    }

    /**
     * 构建分组聚合条件
     *
     * @param condition 查询条件
     * @param groupName 分组名称
     * @return TermsAggregationBuilder 按照分页分组聚合
     */
    private TermsAggregationBuilder buildPageTermsAggregationBuilder(NodeCondition condition, String groupName) {
        TermsAggregationBuilder termsAggregationBuilder =
                this.buildTermsAggregation(condition.getGroupByColumn(), groupName);
        int pageSize = condition.getPageSize();
        int pageNum = condition.getPageNum();
        int from = (pageNum - 1) * pageSize;
        termsAggregationBuilder.size(from + pageSize)
            .subAggregation(new BucketSortPipelineAggregationBuilder(aggregationPageBucket, null)
                .from(from).size(pageSize));
        return termsAggregationBuilder;
    }

    /**
     * 构建分组聚合条件
     *
     * @param groupByColumn 分组字段
     * @param groupName     分组名称
     * @return org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder 分组聚合条件
     */
    private TermsAggregationBuilder buildTermsAggregation(String groupByColumn, String groupName) {
        TermsAggregationBuilder termsAggregationBuilder =
            AggregationBuilders.terms(groupName)
                .field(StringUtil.isEmpty(groupByColumn) ? Metrics.ENTITY_ID : groupByColumn)
                .subAggregation(
                    AggregationBuilders.topHits(topName)
                        .sort(Metrics.TIME_BUCKET, SortOrder.DESC)
                        .fetchSource(fetchSourceFields, Strings.EMPTY_ARRAY)
                        .size(topOne)).size(returnMaxSize);
        return termsAggregationBuilder;
    }

    /**
     * 构建查询条件
     *
     * @param condition 查询条件
     * @return 返回查询匹配对象
     */
    private BoolQueryBuilder buildBaseQueryBuilder(NodeCondition condition) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 时间范围
        if (condition.getEndTime() != 0 && condition.getStartTime() != 0) {
            boolQueryBuilder.must().add(
                QueryBuilders.rangeQuery(Metrics.TIME_BUCKET)
                    .gte(condition.getStartTime())
                    .lte(condition.getEndTime()));
        }

        // 指定索引字段，多值匹配
        if (StringUtil.isNotEmpty(condition.getColumnName())
                && CollectionUtils.isNotEmpty(termValues(condition.getColumnValues()))) {
            boolQueryBuilder.must().add(
                QueryBuilders.termsQuery(
                    condition.getColumnName(), condition.getColumnValues().toArray(new String[0])));
        }

        // 节点名称模糊查询
        if (StringUtil.isNotEmpty(condition.getNameKeyWord())) {
            boolQueryBuilder.must().add(QueryBuilders.wildcardQuery(ConnectionPoolMetric.NAME,
                new StringBuilder(fuzzyMark).append(condition.getNameKeyWord()).append(fuzzyMark).toString()));
        }

        // 查询数据库实例
        if (StringUtil.isNotEmpty(condition.getDatabasePeer())) {
            boolQueryBuilder.must().add(QueryBuilders.wildcardQuery(ConnectionPoolMetric.DATABASE_PEERS,
                new StringBuilder(fuzzyMark).append(condition.getDatabasePeer()).append(fuzzyMark).toString()));
        }
        return boolQueryBuilder;
    }

    /**
     * 去掉列表的空值
     *
     * @param values 值集合
     * @return 去掉空值后的数据
     */
    private List<String> termValues(List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return values;
        }
        return values.stream().filter(StringUtil::isNotEmpty).collect(Collectors.toList());
    }
}

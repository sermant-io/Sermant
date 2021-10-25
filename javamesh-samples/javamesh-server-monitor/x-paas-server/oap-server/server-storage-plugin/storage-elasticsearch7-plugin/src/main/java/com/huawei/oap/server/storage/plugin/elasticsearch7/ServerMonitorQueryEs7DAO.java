/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.oap.server.storage.plugin.elasticsearch7;

import com.huawei.apm.core.drill.BaseReplicator;
import com.huawei.apm.core.drill.DrillThreadLocal;
import com.huawei.apm.core.query.DiskIoMetric;
import com.huawei.apm.core.query.DiskQueryCondition;
import com.huawei.apm.core.query.dao.IServerMonitorQueryDao;

import org.apache.skywalking.apm.util.StringUtil;
import org.apache.skywalking.oap.server.core.analysis.metrics.Metrics;
import org.apache.skywalking.oap.server.storage.plugin.elasticsearch7.Es7DAO;
import org.apache.skywalking.oap.server.storage.plugin.elasticsearch7.client.ElasticSearch7Client;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * server monitor dao
 *
 * @author zhengbin zhao
 * @version 1.0
 * @since 2021-05-15
 */
public class ServerMonitorQueryEs7DAO extends Es7DAO implements IServerMonitorQueryDao {
    /**
     * es磁盘名称的索引字段名称
     */
    private static final String DISK_NAME = "disk_name";

    /**
     * es数值的索引字段名称
     */
    private static final String DISK_VALUE_KEY = "value";

    /**
     * es关键字段索引字段的名称
     */
    private static final String DISK_KEY = "query_id";

    /**
     * base64的服务名称和服务实例名称的连接符
     */
    private static final String BASE64_CONNECTOR = ".1_";

    /**
     * 查询开始条数
     */
    private static final int FROM_VALUE = 0;

    /**
     * 最大查询数量
     */
    private static final int SIZE_VALUE = 10000;

    /**
     * disk io busy索引的前缀
     */
    private static final String DISK_IO_BUSY_INDEX_PREFIX = "server_monitor_disk_iobusy";

    public ServerMonitorQueryEs7DAO(ElasticSearch7Client client) {
        super(client);
    }

    @Override
    public Optional<List<DiskIoMetric>> queryDisk(DiskQueryCondition condition) throws IOException {
        SearchSourceBuilder sourceBuilder = SearchSourceBuilder.searchSource();

        // 构建基本查询条件
        BoolQueryBuilder boolQueryBuilder = this.buildBaseQueryBuilder(condition);
        sourceBuilder.query(boolQueryBuilder).sort(Metrics.TIME_BUCKET, SortOrder.ASC);
        sourceBuilder.from(FROM_VALUE);
        sourceBuilder.size(SIZE_VALUE);
        sourceBuilder.trackTotalHits(true);

        String indexName = condition.getMetricName();
        // huawei update.无损演练：查询新增flag值
        if (DrillThreadLocal.isDrillFlag()) {
            indexName = indexName + BaseReplicator.COPY_FLAG;
        }
        SearchResponse response = getClient().search(indexName, sourceBuilder);

        return analysisEsResponse(response, condition);
    }

    private Optional<List<DiskIoMetric>> analysisEsResponse(SearchResponse response, DiskQueryCondition condition) {
        Map<String, List<Long>> map = new HashMap<>();
        List<DiskIoMetric> list = new ArrayList<>();
        SearchHit[] searchHits = response.getHits().getHits();
        for (SearchHit hit : searchHits) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String key = String.valueOf(sourceAsMap.get(DISK_NAME));
            if (map.containsKey(key)) {
                map.get(key).addAll(generateList(String.valueOf(sourceAsMap.get("time_bucket")), condition,
                    String.valueOf(sourceAsMap.get(DISK_VALUE_KEY)), map.get(key)));
            } else {
                map.put(key, generateList(String.valueOf(sourceAsMap.get("time_bucket")), condition,
                    String.valueOf(sourceAsMap.get(DISK_VALUE_KEY)), map.get(key)));
            }
        }
        for (Map.Entry<String, List<Long>> entry : map.entrySet()) {
            DiskIoMetric diskIoMetric = new DiskIoMetric();
            diskIoMetric.setDiskName(entry.getKey());
            diskIoMetric.setValueList(entry.getValue());
            diskIoMetric.setMetricName(condition.getMetricName());
            list.add(diskIoMetric);
        }
        return Optional.of(list);
    }

    private List<Long> generateList(String selectTime, DiskQueryCondition condition, String value, List<Long> values) {
        String[] strings = condition.getTimeArr();
        List<Long> list = new ArrayList<>();
        long defaultValue = 0L;
        int minListSize = 0;
        int size = values == null ? minListSize : values.size();
        for (int i = size; i < strings.length; i++) {
            if (strings[i].equals(selectTime)) {
                list.add(condition.getMetricName().contains(DISK_IO_BUSY_INDEX_PREFIX)
                    ? doubleToLong(value) : Long.parseLong(value));
                return list;
            } else {
                list.add(defaultValue);
            }
        }
        if (size == minListSize) {
            list.add(defaultValue);
        }
        return list;
    }

    private long doubleToLong(String doubleValue) {
        return new Double(Double.parseDouble(doubleValue)).longValue();
    }

    private BoolQueryBuilder buildBaseQueryBuilder(DiskQueryCondition condition) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 时间范围
        if (Long.parseLong(condition.getEndTime()) != 0 && Long.parseLong(condition.getStartTime()) != 0) {
            boolQueryBuilder.must().add(
                QueryBuilders.rangeQuery(Metrics.TIME_BUCKET)
                    .gte(Long.parseLong(condition.getStartTime()))
                    .lte(Long.parseLong(condition.getEndTime())));
        }

        // 指定索引字段，精确匹配
        if (StringUtil.isNotEmpty(condition.getServiceName())
            && StringUtil.isNotEmpty(condition.getServiceInstanceName())) {
            boolQueryBuilder.must().add(QueryBuilders.matchPhraseQuery(DISK_KEY,
                encode(condition.getServiceName()) + BASE64_CONNECTOR
                    + encode(condition.getServiceInstanceName())));
        }

        return boolQueryBuilder;
    }

    private String encode(String text) {
        return new String(Base64.getEncoder().encode(text.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }
}

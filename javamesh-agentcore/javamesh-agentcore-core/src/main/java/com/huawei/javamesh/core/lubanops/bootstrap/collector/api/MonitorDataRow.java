package com.huawei.javamesh.core.lubanops.bootstrap.collector.api;

import java.util.HashMap;

/**
 * 监控数据的一行，以map形式标示 <br>
 *
 * @author
 * @since 2020年3月13日
 */
public class MonitorDataRow extends HashMap<String, Object> {

    /**
     *
     */
    private static final long serialVersionUID = -2152467628560139889L;

    public MonitorDataRow(int size) {
        super(size);
    }

    public MonitorDataRow() {
        super();
    }

    /**
     * add a data row with fluent style
     *
     * @param rowKey
     * @param rowValue
     * @return
     */
    public MonitorDataRow add(String rowKey, Object rowValue) {
        super.put(rowKey, rowValue);
        return this;
    }

}

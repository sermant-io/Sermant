package com.lubanops.apm.bootstrap.collector.api;

import java.util.Arrays;

/**
 * 代表一行监控数据的主键，这个类重载了hashCode和equals方法，可以当作hash相关的map的key用
 *
 * @author frank.yef
 */
public class PrimaryKey {
    private String[] keys;

    public PrimaryKey(String... pks) {

        if (pks == null || pks.length < 1) {
            throw new RuntimeException("must have at least one");
        }
        keys = pks;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (String s : keys) {
            sb.append(s).append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public String get(int index) {
        return keys[index];
    }

    public int getKeyLength() {
        return keys.length;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.keys);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof PrimaryKey)) {
            return false;
        }
        PrimaryKey pk = (PrimaryKey) o;
        return Arrays.equals(this.keys, pk.keys);
    }

}

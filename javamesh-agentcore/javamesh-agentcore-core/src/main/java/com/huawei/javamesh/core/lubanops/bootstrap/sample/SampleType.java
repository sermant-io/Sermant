package com.huawei.javamesh.core.lubanops.bootstrap.sample;

/**
 * 采样类型
 *
 * @author
 */
public enum SampleType {

    all("1"),
    percentage("2"),
    frequency("3"),
    automatic("4");

    String type;

    SampleType(String t) {
        type = t;
    }

    public String value() {
        return type;
    }

}

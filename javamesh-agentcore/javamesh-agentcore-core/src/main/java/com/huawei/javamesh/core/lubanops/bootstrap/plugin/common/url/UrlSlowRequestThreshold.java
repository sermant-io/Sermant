package com.huawei.javamesh.core.lubanops.bootstrap.plugin.common.url;

import com.huawei.javamesh.core.lubanops.bootstrap.sample.SampleConfig;

public class UrlSlowRequestThreshold extends SampleConfig {

    private String url;

    private Integer slowRequestThreshold;

    public Integer getSlowRequestThreshold() {
        return slowRequestThreshold;
    }

    public void setSlowRequestThreshold(Integer slowRequestThreshold) {
        this.slowRequestThreshold = slowRequestThreshold;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}

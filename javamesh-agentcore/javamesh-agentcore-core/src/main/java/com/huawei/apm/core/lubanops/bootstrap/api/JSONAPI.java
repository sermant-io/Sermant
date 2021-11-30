package com.huawei.apm.core.lubanops.bootstrap.api;

import java.util.List;

public interface JSONAPI {

    String toJSONString(Object object);

    String toJSONString(Object obj, List<String> excludeKeys);

    <T> T parseObject(String text, Class<T> type);

    int[] parseIntArray(String s);

    <T> List<T> parseList(String s, Class<T> type);

}

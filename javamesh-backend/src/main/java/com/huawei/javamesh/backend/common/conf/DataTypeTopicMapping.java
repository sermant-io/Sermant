package com.huawei.javamesh.backend.common.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(
    prefix = "datatype.topic"
)
public class DataTypeTopicMapping {
    private final Map<Integer, String> mapping = new HashMap<>();

    public String getTopicOfType(Integer type) {
        return mapping.get(type);
    }
}

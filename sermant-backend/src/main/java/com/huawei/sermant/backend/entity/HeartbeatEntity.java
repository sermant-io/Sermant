package com.huawei.sermant.backend.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HeartbeatEntity {

   private String app;

   private String hostname;

   private Long heartbeatVersion;

   private String pluginVersion;

   private Long lastHeartbeat;

   private String pluginName;

   private String appType;

   private List<String> ip;

   private String version;
}

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.agent;

import org.springframework.util.StringUtils;

/**
 * 功能描述：
 *
 * @author z30009938
 * @since 2021-10-19
 */
public enum AgentStatus {
    /**
     * Agent就绪状态
     */
    READY("success", "READY"),

    /**
     * Agent忙碌状态
     */
    INACTIVE("running", "BUSY"),

    /**
     * Agent离线状态
     */
    BUSY("pending", "INACTIVE");

    /**
     * 前端展示需要的状态类型
     */
    private final String showStatusType;

    /**
     * 后台真实的状态类型
     */
    private final String realStatusType;

    /**
     * 构造方法初始化每一个枚举值的属性
     *
     * @param showStatusType 前端展示值
     * @param realStatusType 后端实际值
     */
    AgentStatus(String showStatusType, String realStatusType) {
        this.showStatusType = showStatusType;
        this.realStatusType = realStatusType;
    }

    /**
     * 传入的真实agent状态是否能匹配该枚举类型
     *
     * @param realStatusType 枚举类型
     * @return 匹配成功返回true，匹配失败返回false
     */
    public boolean isStatusMatched(String realStatusType) {
        if (StringUtils.isEmpty(realStatusType)) {
            return false;
        }
        return realStatusType.equalsIgnoreCase(realStatusType);
    }

    public String getShowStatusType() {
        return showStatusType;
    }

    public String getRealStatusType() {
        return realStatusType;
    }

    /**
     * 遍历每一个枚举实例，获取对应状态的展示值
     *
     * @param realStatusType 后台服务返回的真实agent状态
     * @return 前端需要的agent状态
     */
    public static String getShowStatus(String realStatusType) {
        for (AgentStatus agentStatus : AgentStatus.values()) {
            if (agentStatus.isStatusMatched(realStatusType)) {
                return agentStatus.showStatusType;
            }
        }
        return "";
    }
}

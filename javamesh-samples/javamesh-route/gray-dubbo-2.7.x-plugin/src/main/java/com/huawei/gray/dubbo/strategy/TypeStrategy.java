/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.strategy;

import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.bootstrap.lubanops.utils.StringUtils;

import java.util.logging.Logger;

/**
 * 规则策略
 *
 * @author pengyuyi
 * @date 2021/10/13
 */
public abstract class TypeStrategy {
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 获取参数值
     *
     * @param arg 参数
     * @param type 获取参数的方式
     * @return 参数值
     */
    public abstract String getValue(Object arg, String type);

    /**
     * 是否匹配规则
     *
     * @param type 规则表达式
     * @return 是否匹配
     */
    public boolean isMatch(String type) {
        if (!checkType(type)) {
            return false;
        }
        try {
            Integer.parseInt(getKey(type));
            return true;
        } catch (NumberFormatException e) {
            LOGGER.warning("type " + type + " is not a number.");
            return false;
        }
    }

    /**
     * 开始字符串
     *
     * @return 开始字符串
     */
    public String getBeginFlag() {
        return "";
    }

    /**
     * 结束字符串
     *
     * @return 结束字符串
     */
    public String getEndFlag() {
        return "";
    }

    /**
     * 检查获取参数的类型
     *
     * @param type 类型
     * @return 是否合法
     */
    public boolean checkType(String type) {
        return StringUtils.isNotBlank(type) && type.startsWith(getBeginFlag()) && type.endsWith(getEndFlag());
    }

    /**
     * 获取参数的key值
     *
     * @param type 获取参数类型
     * @return key值
     */
    public String getKey(String type) {
        return type.substring(getBeginFlag().length(), type.length() - getEndFlag().length());
    }
}

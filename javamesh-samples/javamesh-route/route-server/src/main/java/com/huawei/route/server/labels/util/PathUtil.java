/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.util;

import static com.huawei.route.server.labels.constant.LabelConstant.GENERAL_PAAS;
import static com.huawei.route.server.labels.constant.LabelConstant.SEPARATOR;
import static com.huawei.route.server.labels.constant.LabelConstant.TEMP_STRING;
import static com.huawei.route.server.labels.constant.LabelConstant.VALID;
import static com.huawei.route.server.labels.constant.LabelConstant.XPAAS_LABEL_GROUPS;

/**
 * 标签相关的redis key的工具类
 *
 * @author pengyuyi
 * @date 2021/7/20
 */
public class PathUtil {
    /**
     * 实例标签的redis hash key，值为labelMap
     *
     * @param serviceName 服务名
     * @param instanceName 实例名
     * @param labelGroupName 标签组名
     * @param labelName 标签名
     * @return 实例标签的redis hash key
     */
    public static String getInstanceLabelPath(String serviceName, String instanceName, String labelGroupName,
            String labelName) {
        return GENERAL_PAAS + SEPARATOR + VALID + SEPARATOR + serviceName + SEPARATOR + instanceName + SEPARATOR
                + labelGroupName + SEPARATOR + labelName;
    }

    /**
     * 实例临时标签的redis hash key，值为labelMap
     *
     * @param serviceName 服务名
     * @param instanceName 实例名
     * @param labelGroupName 标签组名
     * @param labelName 标签名
     * @return 实例临时标签的redis hash key
     */
    public static String getInstanceTempLabelPath(String serviceName, String instanceName, String labelGroupName,
            String labelName) {
        return getInstanceLabelPath(serviceName, instanceName, labelGroupName, labelName) + SEPARATOR + TEMP_STRING;
    }

    /**
     * 标签组名的redis hash key，值为：[description: desc, labels: 标签组名集合]
     *
     * @param labelGroupName 标签组名
     * @return 标签组名的redis hash key
     */
    public static String getLabelGroupPath(String labelGroupName) {
        return XPAAS_LABEL_GROUPS + SEPARATOR + labelGroupName;
    }

    /**
     * 标签的redis key，value为label的json字符串
     *
     * @param labelGroupName 标签组名
     * @param labelName 标签名
     * @return 标签的redis key
     */
    public static String getLabelPath(String labelGroupName, String labelName) {
        return getLabelGroupPath(labelGroupName) + SEPARATOR + labelName;
    }
}

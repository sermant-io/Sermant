/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowre.flowreplay.domain.content;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * dubbo构造泛化调用时所需要的全部参数
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-03-11
 */
@Getter
@Setter
public class DubboInvokeContent {
    /**
     * 回放地址
     */
    private String address;

    /**
     * 需要被泛化调用的接口名称
     */
    private String interfaceName;

    /**
     * 接口版本
     */
    private String version;

    /**
     * 接口所属的组
     */
    private String group;

    /**
     * 泛化调用的方法名
     */
    private String methodName;

    /**
     * 参数类型列表
     */
    private String[] parametersTypeList;

    /**
     * 参数列表
     */
    private Object[] parametersList;

    /**
     * 附件列表，key:value
     */
    private Map<String, String> attachments;

    public DubboInvokeContent(String interfaceName, String methodName,
                              String[] parametersTypeList, Object[] parametersList,
                              Map<String, String> attachments) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parametersTypeList = parametersTypeList;
        this.parametersList = parametersList;
        this.attachments = attachments;
    }
}

/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.visibility.entity;

import java.util.List;

/**
 * Methodological information
 *
 * @author zhp
 * @since 2022-11-30
 */
public class MethodInfo {
    /**
     * The name of the method
     */
    private String name;

    /**
     * Participation set
     */
    private List<ParamInfo> paramInfoList;

    /**
     * Return value information
     */
    private ParamInfo returnInfo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ParamInfo> getParamInfoList() {
        return paramInfoList;
    }

    public void setParamInfoList(List<ParamInfo> paramInfoList) {
        this.paramInfoList = paramInfoList;
    }

    public ParamInfo getReturnInfo() {
        return returnInfo;
    }

    public void setReturnInfo(ParamInfo returnInfo) {
        this.returnInfo = returnInfo;
    }
}

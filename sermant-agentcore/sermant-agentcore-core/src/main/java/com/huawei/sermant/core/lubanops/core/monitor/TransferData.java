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

package com.huawei.sermant.core.lubanops.core.monitor;

import java.util.ArrayList;
import java.util.List;

/**
 * transfer信息 <br>
 *
 * @author zWX482523
 * @since 2018年3月3日
 */
public class TransferData {

    private List<String> innerIpList = new ArrayList<String>();

    private List<String> outerIpList = new ArrayList<String>();

    private String host;

    public List<String> getInnerIpList() {
        return innerIpList;
    }

    public void setInnerIpList(List<String> innerIpList) {
        this.innerIpList = innerIpList;
    }

    public void addInnerIp(String innerIp) {
        innerIpList.add(innerIp);
    }

    public List<String> getOuterIpList() {
        return outerIpList;
    }

    public void setOuterIpList(List<String> outerIpList) {
        this.outerIpList = outerIpList;
    }

    public void addOuterIp(String outerIp) {
        outerIpList.add(outerIp);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 根据类型获取ip <br>
     *
     * @param type
     * @return
     * @author zWX482523
     * @since 2018年3月3日
     */
    public List<String> getIpListByType(String type) {
        if ("outer".equals(type)) {
            return outerIpList;
        } else if ("inner".equals(type)) {
            return innerIpList;
        }
        return new ArrayList<String>();
    }

}

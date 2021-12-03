/*
 * Copyright (C) Huawei Technologies Co., Ltd. $YEAR$-$YEAR$. All rights reserved
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

package com.huawei.hercules.controller.monitor.dto;

public class NetworkAddressDTO {
    private String hostname;
    private String address;

    public String getHostname() {
        return hostname;
    }

    public String getAddress() {
        return address;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

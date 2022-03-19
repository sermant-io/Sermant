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

package com.huawei.sermant.core.lubanops.integration.transport.websocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author
 * @since 2020/5/14
 **/
public class ConnectConfig {

    /**
     * 实例Id，一旦下发就不会变
     */
    private Long instanceId;

    /**
     * 连接超时时间
     */
    private long connectTimeout = 2000L;

    /**
     * 是否随机连接
     */
    private boolean randomConnect = false;

    /**
     * 安全的地址列表，取地址的时候内部地址放在前面，先连接内部地址，如果内部地址链接不上，再外部地址
     */
    private List<String> secureAddressList;

    /**
     * 非安全的地址列表，列表地址先内部地址，然后再外部地址
     */
    private List<String> unSecureAddressList;

    /**
     * 是否是安全连接
     */
    private boolean isSecure = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConnectConfig)) {
            return false;
        }

        ConnectConfig that = (ConnectConfig) o;

        if (connectTimeout != that.connectTimeout) {
            return false;
        }
        if (randomConnect != that.randomConnect) {
            return false;
        }
        if (isSecure != that.isSecure) {
            return false;
        }
        if (instanceId != null ? !instanceId.equals(that.instanceId) : that.instanceId != null) {
            return false;
        }
        if (secureAddressList != null
                ? !secureAddressList.equals(that.secureAddressList)
                : that.secureAddressList != null) {
            return false;
        }
        return unSecureAddressList != null
                ? unSecureAddressList.equals(that.unSecureAddressList)
                : that.unSecureAddressList == null;
    }

    @Override
    public int hashCode() {
        int result = instanceId != null ? instanceId.hashCode() : 0;
        result = 31 * result + (int) (connectTimeout ^ (connectTimeout >>> 32));
        result = 31 * result + (randomConnect ? 1 : 0);
        result = 31 * result + (secureAddressList != null ? secureAddressList.hashCode() : 0);
        result = 31 * result + (unSecureAddressList != null ? unSecureAddressList.hashCode() : 0);
        return 31 * result + (isSecure ? 1 : 0);
    }

    /**
     * 获取地址列表
     * @return
     */
    public List<String> getAddressList() {
        List<String> list = new ArrayList<String>();
        if (isSecure) {
            list.addAll(secureAddressList);
        } else {
            list.addAll(unSecureAddressList);
        }
        if (this.isRandomConnect()) { // 如果随机连接，就需要次序打乱
            Collections.shuffle(list);
        }
        return list;
    }

    public boolean isSecure() {
        return isSecure;
    }

    public void setSecure(boolean secure) {
        isSecure = secure;
    }

    public List<String> getSecureAddressList() {
        return secureAddressList;
    }

    public void setSecureAddressList(List<String> secureAddressList) {
        this.secureAddressList = secureAddressList;
    }

    public List<String> getUnSecureAddressList() {
        return unSecureAddressList;
    }

    public void setUnSecureAddressList(List<String> unSecureAddressList) {
        this.unSecureAddressList = unSecureAddressList;
    }

    public boolean isRandomConnect() {
        return randomConnect;
    }

    public void setRandomConnect(boolean randomConnect) {
        this.randomConnect = randomConnect;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }
}

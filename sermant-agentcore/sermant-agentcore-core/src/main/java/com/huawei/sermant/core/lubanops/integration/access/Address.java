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

package com.huawei.sermant.core.lubanops.integration.access;

import com.huawei.sermant.core.lubanops.integration.enums.AddressScope;
import com.huawei.sermant.core.lubanops.integration.enums.AddressType;
import com.huawei.sermant.core.lubanops.integration.enums.Protocol;

/**
 * 服务器地址信息，包括 acesss服务器地址，
 * @author
 * @since 2020/4/9
 **/
public class Address {

    /**
     * 主机名，可以是ip也可以是域名
     */
    private String host;

    /**
     * 端口
     */
    private int port;

    /**
     * 安全端口
     */
    private int sport;

    /**
     * inner或者outer，代表是内网还是外网，地址优先链接内网的
     */
    private AddressType type;

    /**
     * 内外
     */
    private AddressScope scope;

    /*
     * 协议,当前只支持ws
     */
    private Protocol protocol;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public AddressType getType() {
        return type;
    }

    public void setType(AddressType type) {
        this.type = type;
    }

    public AddressScope getScope() {
        return scope;
    }

    public void setScope(AddressScope scope) {
        this.scope = scope;
    }

    public int getSport() {
        return sport;
    }

    public void setSport(int sport) {
        this.sport = sport;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Address other = (Address) obj;
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        if (sport != other.sport) {
            return false;
        }
        if (protocol == null) {
            if (other.protocol != null) {
                return false;
            }
        } else if (!protocol.getValue().equals(other.protocol.getValue())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Address{");
        sb.append("host='").append(host).append('\'');
        sb.append(", port=").append(port);
        sb.append(", sport=").append(sport);
        sb.append(", type=").append(type);
        sb.append(", scope=").append(scope);
        sb.append(", protocol=").append(protocol);
        sb.append('}');
        return sb.toString();
    }
}

/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowcontrol.console.auth;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 此处部分引用alibaba/Sentinel开源社区代码，诚挚感谢alibaba/Sentinel开源团队的慷慨贡献
 */
@Component
public class AuthUserImpl implements AuthUser, Serializable {
    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean authTarget(String target, AuthService.PrivilegeType privilegeType) {
        return false;
    }

    @Override
    public boolean isSuperUser() {
        return true;
    }

    @Override
    public String getNickName() {
        return username;
    }

    @Override
    public String getLoginName() {
        return username;
    }

    @Override
    public String getId() {
        return username;
    }
}

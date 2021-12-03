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

/**
 * 此处部分引用alibaba/Sentinel开源社区代码，诚挚感谢alibaba/Sentinel开源团队的慷慨贡献
 */
public interface AuthUser {
    /**
     * Query whether current user has the specific privilege to the target, the target
     * may be an app name or an ip address, or other destination.
     * <p>
     * This method will use return value to represent  whether user has the specific
     * privileges to the target, but to throw a RuntimeException to represent no auth
     * is also a good way.
     * </p>
     *
     * @param target        the target to check
     * @param privilegeType the privilege type to check
     * @return if current user has the specific privileges to the target, return true,
     * otherwise return false.
     */
    boolean authTarget(String target, AuthService.PrivilegeType privilegeType);

    /**
     * Check whether current user is a super-user.
     *
     * @return if current user is super user return true, else return false.
     */
    boolean isSuperUser();

    /**
     * Get current user's nick name.
     *
     * @return current user's nick name.
     */
    String getNickName();

    /**
     * Get current user's login name.
     *
     * @return current user's login name.
     */
    String getLoginName();

    /**
     * Get current user's ID.
     *
     * @return ID of current user
     */
    String getId();
}

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
public interface AuthService<R> {
    /**
     * Get the authentication user.
     *
     * @param request the request contains the user information
     * @return the auth user represent the current user, when the user is illegal, a null value will return.
     */
    AuthUser getAuthUser(R request);

    /**
     * Privilege type.
     *
     * @author Gaofang Wu
     * @since 2020-11-25
     */
    enum PrivilegeType {
        /**
         * Read rule
         */
        READ_RULE,
        /**
         * Create or modify rule
         */
        WRITE_RULE,
        /**
         * Delete rule
         */
        DELETE_RULE,
        /**
         * Read metrics
         */
        READ_METRIC,
        /**
         * Add machine
         */
        ADD_MACHINE,
        /**
         * All privileges above are granted.
         */
        ALL
    }
}

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.console.auth;

/**
 * 文件名：AuthService
 * 版权：
 * 描述：用户接口类
 *
 * @author Gaofang Wu
 * @param <R> 用户信息
 * @since 2020-11-25
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

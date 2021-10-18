/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.auth;

/**
 * 文件名：AuthUser
 * 版权：
 * 描述：用户bean接口类
 *
 * @author Gaofang Wu
 * @since 2020-11-25
 * 跟踪单号：
 * 修改单号：
 * 修改内容：
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

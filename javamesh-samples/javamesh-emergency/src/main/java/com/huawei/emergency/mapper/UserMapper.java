/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户mapper
 *
 * @author h30009881
 * @since 2021-11-15
 **/
@Mapper
public interface UserMapper {
    List<String> getAuthByRole(String role);

    String getRoleByUserName(String userName);

    String getUserStatus(String userName);
}

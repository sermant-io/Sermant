package com.huawei.emergency.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    List<String> getAuthByRole(String role);

    String getRoleByUserName(String userName);
}

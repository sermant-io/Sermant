package com.huawei.user.mapper;

import com.huawei.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface UserMapper {
    List<String> getAuthByRole(String userName);

    String getRoleByUserName(String userName);

    UserEntity selectUserByName(String userName);

    int countByName(String userName);

    int insertUser(UserEntity entity);

    List<UserEntity> listUser(UserEntity user);

    int updateEnableByName(String[] usernames, String enable, Timestamp timestamp);

    int updatePwdByName(String userName, String password,Timestamp timestamp);

    int updateUser(UserEntity user);

    int insertRole(UserEntity entity);

    void updateRole(UserEntity user);

    String getUserStatus(String userName);
}

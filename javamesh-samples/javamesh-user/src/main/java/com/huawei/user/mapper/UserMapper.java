package com.huawei.user.mapper;

import com.huawei.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    List<String> getAuthByRole(String userName);

    String getRoleByUserName(String userName);

    int changePassword(String userName, String password);

    UserEntity selectUserByName(String userName);

    int countByName(String userName);

    int insertUser(UserEntity entity);

    List<UserEntity> listUser(UserEntity user);

    int updateEnableByName(String[] usernames, String enable);

    int updatePwdByName(String userName, String password);

    int updateUser(UserEntity user);

    int insertRole(UserEntity entity);

    void updateRole(UserEntity user);
}

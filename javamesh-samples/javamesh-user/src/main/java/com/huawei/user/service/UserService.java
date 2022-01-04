package com.huawei.user.service;

import com.alibaba.fastjson.JSONObject;
import com.huawei.user.common.api.CommonResult;
import com.huawei.user.entity.UserEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface UserService {

    JSONObject login(String username, String password, String nativeLanguage, String userTimezone);

    CommonResult getUserInfo(HttpServletRequest request);

    String logout();

    String changePwd(HttpServletRequest request,Map<String, String> param);

    String register(UserEntity entity);

    CommonResult listUser(String nickName, String userName, String role, String status, int pageSize, int current, String sorter, String order);

    String suspend(HttpServletRequest request, String[] usernames);

    String enable(String[] usernames);

    CommonResult addUser(UserEntity user);

    CommonResult resetPwd(UserEntity user);

    String updateUser(UserEntity user);

    String getUserStatus(String userName);
}

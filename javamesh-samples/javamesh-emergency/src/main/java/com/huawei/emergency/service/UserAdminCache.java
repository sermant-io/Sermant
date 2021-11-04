package com.huawei.emergency.service;

import com.huawei.emergency.entity.User;

import java.util.HashMap;
import java.util.Map;

public interface UserAdminCache {
    Map<String, User> userMap = new HashMap<>();
}

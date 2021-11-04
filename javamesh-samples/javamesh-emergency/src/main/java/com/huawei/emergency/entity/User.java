package com.huawei.emergency.entity;

import lombok.Data;

import java.util.List;

@Data
public class User {
    private String nick_name;

    private String userName;

    private String passWord;

    private String role;

    private List<String> auth;
}

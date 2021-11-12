package com.huawei.emergency.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String nickName;

    private String userName;

    private String passWord;

    private String role;

    private List<String> auth;

    public User(String userName, String nickName, String role, List<String> auth) {
        this.userName = userName;
        this.nickName = nickName;
        this.role = role;
        this.auth = auth;
    }
}

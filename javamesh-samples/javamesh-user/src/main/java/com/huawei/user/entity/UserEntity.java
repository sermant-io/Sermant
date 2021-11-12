package com.huawei.user.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @JsonProperty("username")
    private String userName;

    @JsonProperty("nickname")
    private String nickName;

    private String password;

    private String role;

    private List<String> auth;

    @JsonProperty("status")
    private String enabled;

    private Timestamp createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("update_time")
    private Timestamp updateTime;

    public UserEntity(String userName, String nickName, String role, List<String> auth) {
        this.userName = userName;
        this.nickName = nickName;
        this.role = role;
        this.auth = auth;
    }
}

package com.huawei.user.common.constant;

public class FailedInfo {

    public static final String OLD_NEW_PASSWORD_EQUALS = "新老密码一致";
    public static final String CONFIRM_PASSWORD_ERROR = "两次密码不一致";
    public static final String CHANGE_PASSWORD_FAILED = "修改密码失败";
    public static final String PASSWORD_ERROR = "原密码错误";
    public static final String USERNAME_EXISTS = "登录账号已存在";
    public static final String REGISTER_FAILED = "注册失败";
    public static final String SUSPEND_NOT_SELF_OR_ADMIN = "不能禁用自己或admin账户";
    public static final String SUSPEND_NOT_ALL_SUCCESS = "禁用账号未全部成功";
    public static final String SUSPEND_FAIL = "禁用账号失败";
    public static final String ENABLE_FAIL = "启用账号失败";
    public static final String ENABLE_NOT_ALL_SUCCESS = "启用账号未全部成功";
    public static final String ADD_USER_FAIL = "新建用户失败";
    public static final String RESET_PWD_FAIL = "重置密码失败";
    public static final String UPDATE_USER_FAIL = "修改用户失败";
    public static final String ENCODE_PASSWORD_FAIL = "密码加密失败";
    public static final String CANNOT_UPDATE_ADMIN = "不能修改admin用户";

    private FailedInfo() {
    }
}

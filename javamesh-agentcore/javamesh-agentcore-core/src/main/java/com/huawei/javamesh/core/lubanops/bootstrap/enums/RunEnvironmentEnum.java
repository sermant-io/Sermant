package com.huawei.javamesh.core.lubanops.bootstrap.enums;

/**
 * @author
 * @date 2020/8/19 16:35
 */
public enum RunEnvironmentEnum {
    INHUAWEI("inhuawei"),
    HUAWEICLOUD("huaweicloud"),
    ;

    private String code;

    RunEnvironmentEnum(String code) {
        this.code = code;
    }

    public static RunEnvironmentEnum getEnvByCode(String code) {
        RunEnvironmentEnum env = INHUAWEI;
        for (RunEnvironmentEnum val : values()) {
            if (val.code.equals(code)) {
                env = val;
                break;
            }
        }
        return env;
    }
}

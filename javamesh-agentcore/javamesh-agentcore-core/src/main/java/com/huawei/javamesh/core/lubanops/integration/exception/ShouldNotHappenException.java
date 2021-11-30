package com.huawei.javamesh.core.lubanops.integration.exception;

/**
 * 这种类型异常主要是开发用户不遵循规划或者数据库系统有脏数据或者系统配置错误等情况下抛出， 抛出这种异常代表系统出问题 <br>
 *
 * @author
 * @since 2020年3月1日
 */
public class ShouldNotHappenException extends JavaagentRuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 5363743637146702849L;

    public ShouldNotHappenException() {
    }

    public ShouldNotHappenException(String message) {
        super(message);
    }

}

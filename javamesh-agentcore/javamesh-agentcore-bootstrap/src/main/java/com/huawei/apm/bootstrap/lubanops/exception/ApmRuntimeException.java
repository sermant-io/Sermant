package com.huawei.apm.bootstrap.lubanops.exception;

/**
 * 全局通用的运行期异常 <br>
 *
 * @author
 * @since 2020年3月9日
 */
public class ApmRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1778208300580921841L;

    public ApmRuntimeException() {
    }

    public ApmRuntimeException(String message) {
        super(message);
    }

    public ApmRuntimeException(String message, Throwable ex) {
        super(message, ex);
    }

    public ApmRuntimeException(Throwable ex) {
        super(ex);
    }

}

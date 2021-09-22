package com.lubanops.apm.integration.exception;

/**
 * 所有的lubanops平台的 <br>
 *
 * @author
 * @since 2020年2月29日
 */
public class JavaagentRuntimeException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -161240411185863675L;

    public JavaagentRuntimeException() {
    }

    public JavaagentRuntimeException(String message) {
        super(message);
    }

    public JavaagentRuntimeException(String message, Throwable ex) {
        super(message, ex);
    }

    public JavaagentRuntimeException(Throwable ex) {
        super(ex);
    }
}

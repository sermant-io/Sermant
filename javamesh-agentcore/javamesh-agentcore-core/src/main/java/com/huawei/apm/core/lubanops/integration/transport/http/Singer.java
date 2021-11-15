package com.huawei.apm.core.lubanops.integration.transport.http;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author
 * @date 2020/8/7 15:29
 */
public interface Singer {
    static final String SDK_SIGNING_ALGORITHM = "SDK-HMAC-SHA256";
    static final String TIME_FORMATTER = "yyyyMMdd'T'HHmmss'Z'";
    static final String SIGN_NEW_LINE = "\n";
    static final String SIGN_FIELD_HOST = "Host";

    /**
     * 对request数据进行签名，并将签名放入signature字段。
     * @param request
     *            request数据对象。
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    void sign(Request request) throws InvalidKeyException, NoSuchAlgorithmException;

}

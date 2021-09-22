package com.lubanops.apm.integration.transport.http;

import java.io.UnsupportedEncodingException;

/**
 * @author
 * @date 2020/8/7 15:36
 */
public interface Request {

    /**
     * 设置签名。
     *
     * @param signature 签名数据
     */
    void setSignature(String signature);

    /**
     * 生成原生request对象。
     *
     * @return
     * @throws
     */
    Object generate() throws UnsupportedEncodingException;

}

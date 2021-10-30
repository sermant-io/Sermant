/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.route.server.console.filter;

import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * 获取request中流的信息，并将流写回request
 *
 * @author zhengbin zhao
 * @since 2021-04-16
 */
public class SafeHttpServletRequestWrapper extends HttpServletRequestWrapper {
    /**
     * 编码集
     */
    private static final String CHARSET_UTF8 = "UTF-8";

    private final byte[] body;
    private String bodyString;

    public SafeHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.bodyString = StreamUtils.copyToString(request.getInputStream(), Charset.forName(CHARSET_UTF8));
        body = bodyString.getBytes(CHARSET_UTF8);
    }

    public String getBodyString() {
        return this.bodyString;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }
}

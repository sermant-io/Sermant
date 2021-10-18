/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.filter;

import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 获取request中流的信息，并将流写回request
 *
 * @author zhengbin zhao
 * @since 2021-04-16
 */
public class SafeHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private final byte[] body;

    public SafeHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        String bodyString = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        body = bodyString.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public ServletInputStream getInputStream() {
        return new SafeHttpServletInputStream(new ByteArrayInputStream(body));
    }

    static class SafeHttpServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream byteArrayInputStream;

        SafeHttpServletInputStream(ByteArrayInputStream byteArrayInputStream) {
            this.byteArrayInputStream = byteArrayInputStream;
        }

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
        public int read() {
            return byteArrayInputStream.read();
        }
    }
}
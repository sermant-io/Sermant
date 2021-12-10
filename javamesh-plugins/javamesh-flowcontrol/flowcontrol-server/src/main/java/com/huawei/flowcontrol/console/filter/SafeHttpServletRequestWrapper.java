/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

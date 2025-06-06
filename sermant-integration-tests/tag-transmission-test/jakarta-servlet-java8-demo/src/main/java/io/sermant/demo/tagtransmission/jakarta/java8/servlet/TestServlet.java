/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.demo.tagtransmission.jakarta.java8.servlet;

import io.sermant.demo.tagtransmission.jakarta.java8.utils.TagConversionUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * simple servlet
 *
 * @since 2025-05-28
 */
public class TestServlet extends HttpServlet {

    /**
     * traffic tag key
     */
    public static final String[] TRAFFIC_TAG_KEY = new String[]{"id", "dynamic", "x-sermant-test", "tag-sermant"};

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (PrintWriter writer = resp.getWriter()) {
            String responseBody = TagConversionUtils.convertHeader2String(req, TRAFFIC_TAG_KEY);
            writer.println(responseBody);
        }
    }
}

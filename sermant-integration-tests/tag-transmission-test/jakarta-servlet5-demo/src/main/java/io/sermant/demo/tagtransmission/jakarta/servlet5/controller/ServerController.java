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

package io.sermant.demo.tagtransmission.jakarta.servlet5.controller;

import io.sermant.demo.tagtransmission.jakarta.servlet5.utils.TagConversionUtils;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servlet5.x, 6.x server
 *
 * @since 2025-05-22
 */
@RestController
@RequestMapping("jakarta-servlet5")
public class ServerController {
    @Value("${traffic.tag.key}")
    private String[] trafficTagKey;

    /**
     * return traffic tag
     *
     * @param request http request
     * @return traffic tag
     */
    @RequestMapping(value = "httpServer", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String testHttpServer(HttpServletRequest request) {
        return TagConversionUtils.convertHeader2String(request, trafficTagKey);
    }
}

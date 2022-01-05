/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.emergency.controller;

import com.huawei.common.api.CommonPage;
import com.huawei.common.api.CommonResult;
import com.huawei.emergency.entity.EmergencyServer;
import com.huawei.emergency.entity.User;
import com.huawei.emergency.service.EmergencyServerService;
import com.huawei.emergency.service.impl.EmergencyServerServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * @author y30010171
 * @since 2021-12-07
 **/
@RestController
@RequestMapping("/api/host")
public class EmergencyServerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmergencyServerController.class);

    @Autowired
    EmergencyServerService serverService;

    @GetMapping("/search/password_uri")
    public CommonResult allServerUser(@RequestParam(value = "server_ip", required = false) String serverIp) {
        return serverService.allServerUser(serverIp);
    }

    @GetMapping("/search")
    public CommonResult allServerName(@RequestParam(value = "value", required = false) String serverName) {
        return serverService.search(serverName);
    }

    @PostMapping
    public CommonResult createServer(HttpServletRequest request, @RequestBody EmergencyServer server) {
        if ("有".equals(server.getHavePassword())) {
            server.setHavePassword("1");
            if ("平台".equals(server.getPasswordMode())) {
                server.setPasswordMode("1");
            } else if ("本地".equals(server.getPasswordMode())) {
                server.setPasswordMode("0");
            }
        } else if ("无".equals(server.getHavePassword())) {
            server.setHavePassword("0");
        }
        server.setCreateUser(parseUserName(request));
        return serverService.add(server);
    }

    @DeleteMapping
    public CommonResult deleteServer(HttpServletRequest request, @RequestParam(value = "server_id[]", required = false) String[] serverIds) {
        return serverService.deleteServerList(serverIds, parseUserName(request));
    }

    /*@PostMapping
    public CommonResult updateServer(HttpServletRequest request, @RequestBody EmergencyServer server) {
        server.setUpdateUser(parseUserName(request));
        return serverService.update(server);
    }*/

    @PostMapping("/license")
    public CommonResult license(@RequestBody EmergencyServer server) {
        return serverService.license(server);
    }

    @GetMapping
    public CommonResult queryServerInfo(@RequestParam(value = "keywords", required = false) String keyword,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                        @RequestParam(value = "current", defaultValue = "1") int current,
                                        @RequestParam(value = "sorter", defaultValue = "create_time") String sorter,
                                        @RequestParam(value = "order", defaultValue = "DESC") String order,
                                        @RequestParam(value = "server_name", required = false) String serverName,
                                        @RequestParam(value = "excludes[]", required = false) int[] excludeServerIds) {
        CommonPage<EmergencyServer> params = new CommonPage<>();
        params.setPageSize(pageSize);
        params.setPageIndex(current);
        params.setSortField(sorter);
        if ("ascend".equals(order)) {
            params.setSortType("ASC");
        } else if ("descend".equals(order)) {
            params.setSortType("DESC");
        }
        EmergencyServer server = new EmergencyServer();
        server.setServerName(serverName);
        params.setObject(server);
        return serverService.queryServerInfo(params, keyword, excludeServerIds);
    }

    @PostMapping("/stop")
    public CommonResult stop() {
        return CommonResult.success();
    }

    private String parseUserName(HttpServletRequest request) {
        String userName = "";
        try {
            User user = (User) request.getSession().getAttribute("userInfo");
            if (user != null) {
                userName = user.getUserName();
            }
        } catch (Exception e) {
            LOGGER.error("get user info error.", e);
        }
        return userName;
    }
}

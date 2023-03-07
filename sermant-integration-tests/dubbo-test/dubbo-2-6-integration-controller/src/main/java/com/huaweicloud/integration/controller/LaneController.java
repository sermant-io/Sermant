/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.integration.controller;

import com.huaweicloud.integration.constants.Constant;
import com.huaweicloud.integration.entity.LaneTestEntity;
import com.huaweicloud.integration.entity.TestEntity;
import com.huaweicloud.integration.service.LaneService;

import com.alibaba.dubbo.rpc.RpcContext;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 泳道测试
 *
 * @author provenceee
 * @since 2023-03-02
 */
@RestController
@RequestMapping("/controller")
public class LaneController {
    @Resource(name = "laneService")
    private LaneService laneService;

    /**
     * 获取泳道
     *
     * @param entity entity
     * @return map
     */
    @GetMapping("/getLaneByDubbo")
    public Map<String, Object> getLaneByDubbo(TestEntity entity) {
        RpcContext.getContext().setAttachments(getLaneFlag());
        RpcContext.getContext().setAttachment(Constant.LANE_TEST_USER_ID, String.valueOf(entity.getId()));
        return laneService.getLaneByDubbo(entity.getName(), new LaneTestEntity(entity.getLaneId(), entity.isEnabled()),
            new String[]{entity.getArrName()}, Collections.singletonList(entity.getListId()),
            Collections.singletonMap("name", entity.getMapName()));
    }

    /**
     * 获取泳道
     *
     * @param name name
     * @param id id
     * @param enabled enabled
     * @return map
     */
    @GetMapping("/getLaneByFeign")
    public Map<String, Object> getLaneByFeign(@RequestParam(value = "name", defaultValue = "") String name,
        @RequestParam(value = "id", defaultValue = "0") int id,
        @RequestParam(value = "enabled", defaultValue = "false") boolean enabled) {
        RpcContext.getContext().setAttachments(getLaneFlag());
        RpcContext.getContext().setAttachment(Constant.LANE_TEST_USER_ID, String.valueOf(id));
        return laneService.getLaneByFeign(name, new LaneTestEntity(id, enabled));
    }

    /**
     * 获取泳道
     *
     * @param name name
     * @param id id
     * @param enabled enabled
     * @return map
     */
    @GetMapping("/getLaneByRest")
    public Map<String, Object> getLaneByRest(@RequestParam(value = "name", defaultValue = "") String name,
        @RequestParam(value = "id", defaultValue = "0") int id,
        @RequestParam(value = "enabled", defaultValue = "false") boolean enabled) {
        RpcContext.getContext().setAttachments(getLaneFlag());
        RpcContext.getContext().setAttachment(Constant.LANE_TEST_USER_ID, String.valueOf(id));
        return laneService.getLaneByRest(name, new LaneTestEntity(id, enabled));
    }

    private Map<String, String> getLaneFlag() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
            .getRequest();
        Map<String, String> headers = new HashMap<>();
        Enumeration<?> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            if (key.startsWith("x-sermant-")) {
                headers.put(key, enumeration2List(request.getHeaders(key)).get(0));
            }
        }
        return headers;
    }

    private List<String> enumeration2List(Enumeration<?> enumeration) {
        List<String> collection = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            collection.add((String) enumeration.nextElement());
        }
        return collection;
    }
}
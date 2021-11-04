/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.controller;

import com.huawei.common.api.CommonResult;
import com.huawei.common.constant.FailedInfo;
import com.huawei.emergency.dto.ScriptDeleteParam;
import com.huawei.emergency.dto.ScriptInfoDto;
import com.huawei.emergency.entity.EmergencyScript;
import com.huawei.emergency.service.EmergencyScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 脚本管理controller
 *
 * @author h30009881
 * @since 2021-10-14
 */
@RestController
@RequestMapping("/api")
public class EmergencyScriptController {
    @Autowired
    private EmergencyScriptService service;

    @GetMapping("/emergency/script")
    public CommonResult<List<EmergencyScript>> listScript(
            HttpServletRequest request,
            @RequestParam(value = "script_name", required = false) String scriptName,
            @RequestParam(value = "script_user", required = false) String scriptUser,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "current", defaultValue = "1") int current,
            @RequestParam(value = "sorter", defaultValue = "update_time") String sorter,
            @RequestParam(value = "order", defaultValue = "DESC") String order) {
        return service.listScript(request, scriptName, scriptUser, pageSize, current, sorter, order);
    }

    /**
     * 脚本删除
     *
     * @param scriptDeleteParam
     * @return {@link CommonResult}
     */
    @PostMapping("/script/delete")
    public CommonResult deleteScript(@RequestBody ScriptDeleteParam scriptDeleteParam) {
        Object[] data = scriptDeleteParam.getData();
        int size = data.length;
        int count = service.deleteScripts(data);
        if (count <= 0) {
            return CommonResult.failed(FailedInfo.DELETE_FAILED);
        } else if (size != count) {
            return CommonResult.failed(FailedInfo.DELETE_NOT_SUCCESS_ALL);
        } else {
            return CommonResult.success(count);
        }
    }

    /**
     * 脚本下载
     *
     * @param scriptId
     * @param response
     */
    @GetMapping("/script/download")
    public void downloadScript(@RequestParam(value = "script_id") int scriptId, HttpServletResponse response) {
        service.downloadScript(scriptId, response);
    }

    /**
     * 上传文件
     *
     * @param file
     * @return {@link CommonResult} 上传结果
     */
    @PostMapping("/script/upload")
    public CommonResult uploadScript(@RequestParam(value = "file") MultipartFile file) {
        Map<String, String> map = service.uploadScript(file);
        if (map == null || map.size() == 0) {
            return CommonResult.failed(FailedInfo.UPLOAD_FAIL);
        } else {
            return CommonResult.success(map);
        }
    }

    /**
     * 获取脚本实例
     *
     * @param scriptId
     * @return CommonResult 脚本信息
     */
    @GetMapping("/script/get")
    public CommonResult<EmergencyScript> selectScript(@RequestParam(value = "script_id") int scriptId) {
        EmergencyScript script = service.selectScript(scriptId);
        return CommonResult.success(script);
    }

}

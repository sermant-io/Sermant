/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.controller;

import com.huawei.common.api.CommonResult;
import com.huawei.common.constant.FailedInfo;
import com.huawei.common.constant.ResultCode;
import com.huawei.emergency.entity.EmergencyScript;
import com.huawei.emergency.service.EmergencyScriptService;
import com.huawei.script.exec.log.LogResponse;
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

    private static final String SUCCESS = "success";

    @GetMapping("/script")
    public CommonResult<List<EmergencyScript>> listScript(
            HttpServletRequest request,
            @RequestParam(value = "script_name", required = false) String scriptName,
            @RequestParam(value = "owner", required = false) String scriptUser,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "current", defaultValue = "1") int current,
            @RequestParam(value = "sorter", required = false) String sorter,
            @RequestParam(value = "order", required = false) String order,
            @RequestParam(value = "status",required = false)String status) {
        return service.listScript(request, scriptName, scriptUser, pageSize, current, sorter, order,status);
    }

    /**
     * 脚本删除
     *
     * @param scriptId
     * @return {@link CommonResult}
     */
    @DeleteMapping("/script")
    public CommonResult deleteScript(@RequestParam(value = "script_id") int[] scriptId) {
        int size = scriptId.length;
        int count = service.deleteScripts(scriptId);
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
    public CommonResult uploadScript(HttpServletRequest request,
                                     @RequestParam(value = "script_name") String scriptName,
                                     @RequestParam(value = "submit_info") String submitInfo,
                                     @RequestParam(value = "account", required = false) String serverUser,
                                     @RequestParam(value = "server_ip") String serverIp,
                                     @RequestParam(value = "has_pwd") String havePassword,
                                     @RequestParam(value = "language") String scriptType,
                                     @RequestParam(value = "param", required = false) String param,
                                     @RequestParam(value = "public") String isPublic,
                                     @RequestParam(value = "pwd", required = false) String password,
                                     @RequestParam(value = "pwd_from", required = false) String passwordMode,
                                     @RequestParam(value = "file") MultipartFile file) {
        EmergencyScript script = new EmergencyScript();
        script.setScriptName(scriptName);
        script.setSubmitInfo(submitInfo);
        script.setServerUser(serverUser);
        script.setServerIp(serverIp);
        script.setHavePassword(havePassword);
        script.setScriptType(scriptType);
        script.setParam(param);
        script.setIsPublic(isPublic);
        script.setPassword(password);
        script.setPasswordMode(passwordMode);
        int result = service.uploadScript(request, script, file);
        if (result == ResultCode.SCRIPT_NAME_EXISTS) {
            return CommonResult.failed(FailedInfo.SCRIPT_NAME_EXISTS);
        } else if (result == ResultCode.PARAM_INVALID) {
            return CommonResult.failed(FailedInfo.PARAM_INVALID);
        } else if (result == ResultCode.FAIL || result == 0) {
            return CommonResult.failed(FailedInfo.SCRIPT_CREATE_FAIL);
        } else {
            return CommonResult.success(result);
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
        if (script == null) {
            return CommonResult.failed(FailedInfo.SCRIPT_NOT_EXISTS);
        }
        return CommonResult.success(script);
    }

    @PostMapping("/script")
    @ResponseBody
    public CommonResult insertScript(HttpServletRequest request, @RequestBody EmergencyScript script) {
        int result = service.insertScript(request, script);
        if (result == ResultCode.SCRIPT_NAME_EXISTS) {
            return CommonResult.failed(FailedInfo.SCRIPT_NAME_EXISTS);
        } else if (result == ResultCode.PARAM_INVALID) {
            return CommonResult.failed(FailedInfo.PARAM_INVALID);
        } else if (result == ResultCode.FAIL) {
            return CommonResult.failed(FailedInfo.SCRIPT_CREATE_FAIL);
        } else {
            return CommonResult.success(result);
        }
    }

    @PutMapping("/script")
    public CommonResult updateScript(HttpServletRequest request, @RequestBody EmergencyScript script) {
        int count = service.updateScript(request, script);
        if (count == 1) {
            return CommonResult.success(count);
        } else if (count == ResultCode.SCRIPT_NAME_EXISTS) {
            return CommonResult.failed(FailedInfo.SCRIPT_NAME_EXISTS);
        } else {
            return CommonResult.failed("文件修改失败");
        }
    }

    @GetMapping("/script/search")
    public CommonResult searchScript(HttpServletRequest request,
                                     @RequestParam(value = "value", required = false) String scriptName,
                                     @RequestParam(value = "status",required = false)String status) {
        List<String> scriptNames = service.searchScript(request, scriptName,status);
        return CommonResult.success(scriptNames);
    }

    @GetMapping("/script/getByName")
    public CommonResult getScriptEntityByName(@RequestParam(value = "name") String scriptName) {
        EmergencyScript script = service.getScriptByName(scriptName);
        return CommonResult.success(script);
    }

    @PostMapping("/script/submitReview")
    public CommonResult submitReview(HttpServletRequest request, @RequestBody EmergencyScript script) {
        String result = service.submitReview(request, script);
        if (result.equals(SUCCESS)) {
            return CommonResult.success(result);
        } else {
            return CommonResult.failed(result);
        }
    }

    @PostMapping("/script/approve")
    public CommonResult approve(@RequestBody Map<String, Object> map) {
        int count = service.approve(map);
        if(count == 0){
            return CommonResult.failed(FailedInfo.APPROVE_FAIL);
        } else{
            return CommonResult.success(SUCCESS);
        }
    }

    @PostMapping("/script/debug")
    public CommonResult debugScript(@RequestBody Map<String,Integer> param) {
        return service.debugScript(param.get("script_id"));
    }

    @GetMapping("/script/debugLog")
    public LogResponse debugLog(@RequestParam(value = "debug_id") int id,
                                @RequestParam(value = "line", defaultValue = "1") int lineNum) {
        int lineIndex = lineNum;
        if (lineIndex <= 0) {
            lineIndex = 1;
        }
        return service.debugLog(id,lineIndex);
    }

}

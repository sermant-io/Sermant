/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.controller;

import com.huawei.common.api.CommonResult;
import com.huawei.common.constant.FailedInfo;
import com.huawei.emergency.dto.FolderParam;
import com.huawei.emergency.dto.ScriptDeleteParam;
import com.huawei.emergency.dto.ScriptInfoDto;
import com.huawei.emergency.dto.ScriptListDto;
import com.huawei.emergency.dto.ScriptListParam;
import com.huawei.emergency.dto.SearchScriptDto;
import com.huawei.emergency.dto.UpdateScriptParam;
import com.huawei.emergency.entity.ScriptEntity;
import com.huawei.emergency.service.ScriptService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * 脚本管理controller
 *
 * @author h30009881
 * @since 2021-10-14
 */
@RestController
@RequestMapping("/api")
public class ScriptController {
    @Value("${user_name}")
    private String userName;

    @Autowired
    private ScriptService scriptService;

    /**
     * 脚本列表查询
     *
     * @param keywords
     * @param scriptName
     * @param submitInfo
     * @param folderId
     * @param pageSize
     * @param current
     * @param sorter
     * @param order
     * @return {@link CommonResult}
     */
    @GetMapping("/script")
    public CommonResult<List<ScriptListDto>> listScript(
        @RequestParam(value = "keywords", required = false) String keywords,
        @RequestParam(value = "script_name[]", required = false) String[] scriptName,
        @RequestParam(value = "submit_info[]", required = false) String[] submitInfo,
        @RequestParam(value = "folder_id", defaultValue = "0") int folderId,
        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
        @RequestParam(value = "current", defaultValue = "1") int current,
        @RequestParam(value = "sorter", defaultValue = "update_time") String sorter,
        @RequestParam(value = "order", defaultValue = "DESC") String order) {
        ScriptListParam param = new ScriptListParam(
            userName, keywords, scriptName, submitInfo, folderId, pageSize, current, sorter, order);
        return scriptService.listScript(param);
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
        int count = scriptService.deleteScripts(data);
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
        scriptService.downloadScript(scriptId, response);
    }

    /**
     * 创建文件夹
     *
     * @param folderParam
     * @return {@link CommonResult}
     */
    @PostMapping("/script/folder")
    public CommonResult createFolder(@RequestBody FolderParam folderParam) {
        folderParam.setUserName(userName);
        int count = scriptService.createFolder(folderParam);
        if (count == 0) {
            return CommonResult.failed("创建失败");
        } else {
            return CommonResult.success(count);
        }
    }

    /**
     * 上传文件
     *
     * @param folderId
     * @param submitInfo
     * @param file
     * @return {@link CommonResult} 上传结果
     */
    @PostMapping("/script/upload")
    public CommonResult uploadScript(@RequestParam(value = "folder_id", required = false) String folderId,
                                     @RequestParam(value = "submit_info") String submitInfo,
                                     @RequestParam(value = "file") MultipartFile file) {
        int folderIdInt = "undefined".equals(folderId) ? 0 : Integer.parseInt(folderId);
        int count = scriptService.uploadScript(folderIdInt, submitInfo, file, userName);
        if (count == 1) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed("文件上传失败");
        }
    }

    /**
     * 获取脚本实例
     *
     * @param scriptId
     * @return CommonResult 脚本信息
     */
    @GetMapping("/script/get")
    public CommonResult<ScriptInfoDto> selectScript(@RequestParam(value = "script_id") int scriptId) {
        ScriptInfoDto script = scriptService.selectScript(scriptId);
        return CommonResult.success(script);
    }

    @PostMapping("/script")
    @ResponseBody
    public CommonResult insertScript(@RequestBody @Validated ScriptEntity script) {
        script.setUserName(userName);
        int count = scriptService.insertScript(script);
        if (count == 0) {
            return CommonResult.failed("新建失败");
        }
        return CommonResult.success(count);
    }

    @PutMapping("/script")
    public CommonResult updateScript(@RequestBody UpdateScriptParam scriptParam) {
        int count = scriptService.updateScript(
            scriptParam.getScriptId(), scriptParam.getSubmitInfo(), scriptParam.getContext(), userName);
        if (count == 1) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed("文件修改失败");
        }
    }

    @GetMapping("/script/search")
    public CommonResult searchScript(@RequestParam(value = "value", required = false) String scriptName) {
        List<SearchScriptDto> searchScripts = scriptService.searchScript(scriptName, userName);
        return CommonResult.success(searchScripts);
    }

    @GetMapping("/script/getByName")
    public CommonResult getScriptEntityByName(@RequestParam(value = "name") String scriptNameAndUser) {
        ScriptInfoDto scriptInfoDto = scriptService.getScriptEntityByName(scriptNameAndUser);
        return CommonResult.success(scriptInfoDto);
    }
}

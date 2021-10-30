/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service;

import com.huawei.common.api.CommonResult;
import com.huawei.emergency.dto.FolderParam;
import com.huawei.emergency.dto.ScriptInfoDto;
import com.huawei.emergency.dto.ScriptListDto;
import com.huawei.emergency.dto.ScriptListParam;
import com.huawei.emergency.dto.SearchScriptDto;
import com.huawei.emergency.entity.ScriptEntity;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * 脚本管理接口
 *
 * @since 2021-10-30
 **/
public interface ScriptService {
    /**
     * 脚本和文件夹列表
     *
     * @param param 创建用户
     * @return 脚本和文件夹列表集合
     */
    CommonResult<List<ScriptListDto>> listScript(ScriptListParam param);

    int insertScript(ScriptEntity scriptEntity);

    ScriptInfoDto selectScript(int scriptId);

    int deleteScripts(Object[] scriptDeleteParams);

    void downloadScript(int scriptId, HttpServletResponse response);

    int createFolder(FolderParam folderParam);

    int uploadScript(int folderId, String submitInfo, MultipartFile file,String userName);

    int updateScript(int scriptId, String submitInfo, String context,String userName);

    /**
     * 根据脚本名和当前系统用户寻找脚本
     *
     * @param scriptName 脚本名
     * @param userName 当前系统用户
     * @return {@link SearchScriptDto}的集合
     */
    List<SearchScriptDto> searchScript(String scriptName, String userName);

    ScriptInfoDto getScriptEntityByName(String scriptNameAndUser);
}

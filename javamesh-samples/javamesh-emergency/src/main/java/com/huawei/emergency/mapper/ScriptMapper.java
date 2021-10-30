/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.mapper;

import com.huawei.emergency.dto.FolderParam;
import com.huawei.emergency.dto.ScriptListDto;
import com.huawei.emergency.dto.ScriptListParam;
import com.huawei.emergency.entity.FolderEntity;
import com.huawei.emergency.entity.ScriptEntity;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

/**
 * 脚本管理Mapper
 *
 * @author h30009881
 * @since 2021-10-13
 */
@Mapper
@Component
public interface ScriptMapper {
    /**
     * 脚本和文件夹列表
     *
     * @param param 创建用户
     * @return 脚本和文件夹列表集合
     */
    List<ScriptListDto> listScript(ScriptListParam param);

    /**
     * 新建脚本
     *
     * @param scriptEntity 脚本对象
     * @return 插入成功的记录数量
     */
    int insertScript(ScriptEntity scriptEntity);

    ScriptEntity selectScriptById(int scriptId);

    int deleteScriptById(int id);

    int deleteFolderById(int id);

    int createFolder(FolderParam folderParam);

    FolderEntity selectFolderById(int folderId);

    void updateFolderTimeById(int id, Timestamp timestamp);

    List<ScriptEntity> searchScript(String scriptName, String userName,int count);

    ScriptEntity getScriptEntityByName(String scriptName, String userName,int count);

    int countScript();
}

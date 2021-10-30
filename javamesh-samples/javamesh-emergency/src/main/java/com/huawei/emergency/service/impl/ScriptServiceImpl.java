/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.service.impl;

import com.huawei.common.api.CommonResult;
import com.huawei.common.constant.ResultCode;
import com.huawei.common.exception.ApiException;
import com.huawei.common.util.FileUtil;
import com.huawei.common.util.PageUtil;
import com.huawei.emergency.dto.FolderParam;
import com.huawei.emergency.dto.ScriptInfoDto;
import com.huawei.emergency.dto.ScriptListDto;
import com.huawei.emergency.dto.ScriptListParam;
import com.huawei.emergency.dto.SearchScriptDto;
import com.huawei.emergency.entity.FolderEntity;
import com.huawei.emergency.entity.ScriptEntity;
import com.huawei.emergency.mapper.ScriptMapper;
import com.huawei.emergency.service.ScriptService;

import com.github.pagehelper.PageHelper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * 脚本管理接口实现类
 *
 * @since 2021-10-30
 **/
@Slf4j
@Service
@Transactional
public class ScriptServiceImpl implements ScriptService {
    private static final int BUF_SIZE = 1024;

    @Autowired
    ScriptMapper mapper;

    @Override
    public CommonResult<List<ScriptListDto>> listScript(ScriptListParam param) {
        if (param.getOrder().equals("ascend")) {
            param.setOrder("ASC");
        } else {
            param.setOrder("DESC");
        }
        PageHelper.orderBy(param.getSorter() + System.lineSeparator() + param.getOrder());
        List<ScriptListDto> scriptLists = mapper.listScript(param);
        for (ScriptListDto dto : scriptLists) {
            dto.setUid(dto.getType() + dto.getScriptId());
        }
        return CommonResult.success(PageUtil.startPage(scriptLists, param.getCurrent(), param.getPageSize()),
            scriptLists.size());
    }

    @Override
    public int insertScript(ScriptEntity scriptEntity) {
        return mapper.insertScript(scriptEntity);
    }

    @Override
    public ScriptInfoDto selectScript(int scriptId) {
        ScriptEntity entity = mapper.selectScriptById(scriptId);
        return new ScriptInfoDto(entity.getScriptName(), entity.getSubmitInfo(), entity.getContext());
    }

    @Override
    public int deleteScripts(Object[] scriptDeleteParams) {
        Map<String, Object> map;
        int count = 0;
        for (Object obj : scriptDeleteParams) {
            map = (Map<String, Object>) obj;
            Integer id = (Integer) map.get("script_id");
            String type = (String) map.get("type");
            if (type == null || id == null) {
                return ResultCode.FAIL;
            }
            switch (type) {
                case "file":
                    count += mapper.deleteScriptById(id);
                    break;
                case "folder":
                    count += mapper.deleteFolderById(id);
                    break;
                default:
                    break;
            }
        }
        return count;
    }

    @Override
    public void downloadScript(int scriptId, HttpServletResponse response) throws ApiException {
        ScriptEntity scriptEntity = mapper.selectScriptById(scriptId);
        if (scriptEntity == null) {
            log.error("ScriptId not exists. ");
            return;
        }
        String scriptName = scriptEntity.getScriptName();
        String context = scriptEntity.getContext();
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(scriptName, "UTF-8"));
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            inputStream = new ByteArrayInputStream(context.getBytes(StandardCharsets.UTF_8));
            byte[] buf = new byte[BUF_SIZE];
            int length = 0;
            outputStream = response.getOutputStream();
            while ((length = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, length);
            }
        } catch (IOException e) {
            log.error("Download script failed. ");
            throw new ApiException("下载文件失败");
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("Close stream failed");
            }
        }
    }

    @Override
    public int createFolder(FolderParam folderParam) {
        int count = mapper.createFolder(folderParam);
        int folderId = folderParam.getFolderId();
        FolderEntity folderEntity = mapper.selectFolderById(folderId);
        updateParentFolderTime(folderEntity.getParentId(), folderEntity.getUpdateTime());
        return count;
    }

    @Override
    public int uploadScript(int folderId, String submitInfo, MultipartFile file, String userName) {
        try {
            InputStream inputStream = file.getInputStream();
            String context = FileUtil.streamToString(inputStream);
            String fileName = file.getResource().getFilename();

            ScriptEntity entity = new ScriptEntity(fileName, submitInfo, context, userName, folderId);
            int count = mapper.insertScript(entity);

            // 插入新数据后修改父文件夹最后修改时间
            int scriptId = entity.getScriptId();
            Timestamp updateTime = mapper.selectScriptById(scriptId).getUpdateTime();
            updateParentFolderTime(folderId, updateTime);
            return count;
        } catch (IOException e) {
            log.error("Exception occurs. Exception info:{}. ", e.getMessage());
            throw new ApiException("文件上传失败");
        }
    }

    private void updateParentFolderTime(int folderId, Timestamp timestamp) {
        if (folderId == 0) {
            return;
        }
        boolean hasParent = true;
        FolderEntity folderEntity = null;

        // 当前文件夹ID
        int id = 0;

        // 父文件夹ID
        int parentId = folderId;
        while (hasParent) {
            folderEntity = mapper.selectFolderById(parentId);
            id = folderEntity.getFolderId();
            parentId = folderEntity.getParentId();
            mapper.updateFolderTimeById(id, timestamp);
            if (parentId == 0) {
                hasParent = false;
            }
        }
    }

    @Override
    public int updateScript(int scriptId, String submitInfo, String context, String userName) {
        ScriptEntity entity = mapper.selectScriptById(scriptId);
        int folderId = entity.getFolderId();
        ScriptEntity newEntity = new ScriptEntity(entity.getScriptName(), submitInfo, context, userName, folderId);
        int count = mapper.insertScript(newEntity);

        // 插入新数据后修改父文件夹最后修改时间
        int id = newEntity.getScriptId();
        Timestamp updateTime = mapper.selectScriptById(id).getUpdateTime();
        updateParentFolderTime(folderId, updateTime);
        return count;
    }

    @Override
    public List<SearchScriptDto> searchScript(String scriptName, String userName) {
        int count = mapper.countScript();
        List<ScriptEntity> entities = mapper.searchScript(scriptName, userName, count);
        List<SearchScriptDto> scriptDto = new ArrayList<>();
        for (ScriptEntity entity : entities) {
            SearchScriptDto searchScriptDto = new SearchScriptDto();
            searchScriptDto.setScriptId(entity.getScriptId());
            searchScriptDto.setScriptNameAndUserName(entity.getScriptName() + "(" + entity.getUserName() + ")");
            searchScriptDto.setContext(entity.getContext());
            searchScriptDto.setSubmitInfo(entity.getSubmitInfo());
            scriptDto.add(searchScriptDto);
        }
        return scriptDto;
    }

    @Override
    public ScriptInfoDto getScriptEntityByName(String scriptNameAndUser) {
        String[] scriptNameAndUsers = scriptNameAndUser.split("\\(");
        String scriptName = scriptNameAndUsers[0];
        String userName = scriptNameAndUsers[1].substring(0, scriptNameAndUsers[1].length() - 1);
        int count = mapper.countScript();
        ScriptEntity entity = mapper.getScriptEntityByName(scriptName, userName, count);
        return new ScriptInfoDto(entity.getScriptName(), entity.getSubmitInfo(), entity.getContext());
    }
}

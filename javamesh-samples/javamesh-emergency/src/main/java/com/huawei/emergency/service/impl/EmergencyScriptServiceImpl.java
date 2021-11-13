package com.huawei.emergency.service.impl;

import com.github.pagehelper.PageHelper;
import com.huawei.common.api.CommonResult;
import com.huawei.common.constant.FailedInfo;
import com.huawei.common.constant.ResultCode;
import com.huawei.common.exception.ApiException;
import com.huawei.common.util.FileUtil;
import com.huawei.common.util.PageUtil;
import com.huawei.common.util.PasswordUtil;
import com.huawei.emergency.entity.EmergencyScript;
import com.huawei.emergency.entity.EmergencyScriptExample;
import com.huawei.emergency.entity.User;
import com.huawei.emergency.mapper.EmergencyScriptMapper;
import com.huawei.emergency.service.EmergencyExecService;
import com.huawei.emergency.service.EmergencyScriptService;
import com.huawei.script.exec.log.LogRespone;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class EmergencyScriptServiceImpl implements EmergencyScriptService {
    private static final int BUF_SIZE = 1024;

    private static final String TYPE_ZERO = "0";

    private static final String TYPE_ONE = "1";

    private static final String TYPE_TWO = "2";

    private static final String SUCCESS = "success";
    private static final String TYPE_THREE = "3";

    private static final String PUBLIC = "公有";
    private static final String PRIVATE = "私有";
    private static final String NO_PASSWORD = "无";
    private static final String HAVE_PASSWORD = "有";
    private static final String ADD = "新增";
    private static final String APPROVING = "待审核";
    private static final String APPROVED = "已审核";
    private static final String UNAPPROVED = "驳回";
    private static final String AUTH_ADMIN = "admin";
    private static final String AUTH_APPROVER = "approver";


    @Autowired
    PasswordUtil passwordUtil;

    @Autowired
    private EmergencyScriptMapper mapper;

    @Autowired
    private EmergencyExecService execService;

    @Override
    public CommonResult<List<EmergencyScript>> listScript(HttpServletRequest request, String scriptName, String scriptUser, int pageSize, int current, String sorter, String order) {
        User user = (User) request.getSession().getAttribute("userInfo");
        String auth;
        List<String> userAuth = user.getAuth();
        if (userAuth.contains(AUTH_ADMIN)) {
            auth = AUTH_ADMIN;
        } else if (userAuth.contains(AUTH_APPROVER)) {
            auth = AUTH_APPROVER;
        } else {
            auth = "";
        }
        if (order.equals("ascend")) {
            PageHelper.orderBy(sorter + System.lineSeparator() + "ASC");
        } else {
            PageHelper.orderBy(sorter + System.lineSeparator() + "DESC");
        }
        List<EmergencyScript> emergencyScripts = mapper.listScript(user.getUserName(), auth, scriptName, scriptUser);
        String scriptStatus;
        for (EmergencyScript script : emergencyScripts) {
            scriptStatus = script.getScriptStatus();
            switch (scriptStatus) {
                case "0":
                    script.setScriptStatus("unapproved");
                    script.setStatusLabel(ADD);
                    break;
                case "1":
                    script.setScriptStatus("approving");
                    script.setStatusLabel(APPROVING);
                    break;
                case "2":
                    script.setScriptStatus("approved");
                    script.setStatusLabel(APPROVED);
                    break;
                default:
                    script.setScriptStatus("unapproved");
                    script.setStatusLabel(UNAPPROVED);
            }
        }
        return CommonResult.success(PageUtil.startPage(emergencyScripts, current, pageSize), emergencyScripts.size());
    }

    @Override
    public int deleteScripts(int[] scriptIds) {
        int count = 0;
        for (int scriptId : scriptIds) {
            count += mapper.deleteByPrimaryKey(scriptId);
        }
        return count;
    }

    @Override
    public void downloadScript(int scriptId, HttpServletResponse response) {
        EmergencyScript emergencyScript = mapper.selectByPrimaryKey(scriptId);
        if (emergencyScript == null) {
            log.error("ScriptId not exists. ");
            return;
        }
        String scriptName = emergencyScript.getScriptName();
        String content = emergencyScript.getContent();
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(scriptName, "UTF-8"));
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            byte[] buf = new byte[BUF_SIZE];
            int length = 0;
            outputStream = response.getOutputStream();
            while ((length = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, length);
            }
        } catch (IOException e) {
            log.error("Download script failed. ");
            throw new ApiException(FailedInfo.DOWNLOAD_SCRIPT_FAIL);
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
    public int uploadScript(HttpServletRequest request, EmergencyScript script, MultipartFile file) {
        if ("undefined".equals(script.getServerIp())) {
            script.setServerIp(null);
        }
        if ("undefined".equals(script.getParam())) {
            script.setParam(null);
        }
        try {
            InputStream inputStream = file.getInputStream();
            String content = FileUtil.streamToString(inputStream);
            script.setContent(content);
            return insertScript(request, script);
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    @Override
    public EmergencyScript selectScript(int scriptId) {
        EmergencyScript scriptInfo = mapper.getScriptInfo(scriptId);
        try {
            if (scriptInfo.getHavePassword().equals("有") && scriptInfo.getPasswordMode().equals("本地")) {
                scriptInfo.setPassword(passwordUtil.decodePassword(scriptInfo.getPassword()));
            }
        } catch (UnsupportedEncodingException e) {
            throw new ApiException("Failed to decode password. ", e);
        }
        return scriptInfo;
    }

    @Override
    public int insertScript(HttpServletRequest request, EmergencyScript script) {
        if (isParamInvalid(script)) {
            return ResultCode.PARAM_INVALID;
        }
        EmergencyScriptExample example = new EmergencyScriptExample();
        example.createCriteria().andScriptNameEqualTo(script.getScriptName());
        long count = mapper.countByExample(example);
        if (count > 0) {
            return ResultCode.SCRIPT_NAME_EXISTS;
        }
        User user = (User) request.getSession().getAttribute("userInfo");
        script.setScriptUser(user.getUserName());
        extracted(script);
        count = mapper.insertSelective(script);
        if (count != 1) {
            return ResultCode.FAIL;
        }
        return script.getScriptId();
    }

    @Override
    public int updateScript(HttpServletRequest request, EmergencyScript script) {
        if (isParamInvalid(script)) {
            return ResultCode.PARAM_INVALID;
        }

        // 脚本名是否修改了
        String oldScriptName = mapper.selectScriptNameById(script.getScriptId());
        String scriptName = script.getScriptName();
        if (!oldScriptName.equals(scriptName)) {
            EmergencyScriptExample example = new EmergencyScriptExample();
            example.createCriteria().andScriptNameEqualTo(scriptName);
            long count = mapper.countByExample(example);
            if (count > 0) {
                return ResultCode.SCRIPT_NAME_EXISTS;
            }
        }
        extracted(script);
        return mapper.updateByPrimaryKeySelective(script);
    }

    @Override
    public List<String> searchScript(HttpServletRequest request, String scriptName) {
        User user = (User) request.getSession().getAttribute("userInfo");
        String userName = user.getUserName();
        String auth = user.getAuth().contains("admin") ? "admin" : "";
        return mapper.searchScript(scriptName, userName, auth);
    }

    @Override
    public EmergencyScript getScriptByName(String scriptName) {
        EmergencyScript scriptInfo = mapper.getScriptByName(scriptName);
        try {
            if (scriptInfo.getHavePassword().equals(HAVE_PASSWORD) && scriptInfo.getPasswordMode().equals("local")) {
                scriptInfo.setPassword(passwordUtil.decodePassword(scriptInfo.getPassword()));
            }
        } catch (UnsupportedEncodingException e) {
            throw new ApiException("Failed to decode password. ", e);
        }
        return scriptInfo;
    }

    @Override
    public String submitReview(HttpServletRequest request, EmergencyScript script) {
        script.setScriptStatus(TYPE_ONE);
        User user = (User) request.getSession().getAttribute("userInfo");
        int count;
        if (user.getAuth().contains("admin") || user.getUserName().equals(mapper.selectUserById(script.getScriptId()))) {
            count = mapper.updateByPrimaryKeySelective(script);
        } else {
            return FailedInfo.INSUFFICIENT_PERMISSIONS;
        }
        if (count == 1) {
            return SUCCESS;
        } else {
            return FailedInfo.SUBMIT_REVIEW_FAIL;
        }
    }

    @Override
    public int approve(Map<String, Object> map) {
        String approve = (String) map.get("approve");
        int scriptId = (int) map.get("script_id");
        EmergencyScript script = new EmergencyScript();
        script.setScriptId(scriptId);
        if (approve.equals("通过")) {
            script.setScriptStatus(TYPE_TWO);
        } else {
            script.setScriptStatus(TYPE_THREE);
            String comment = (String) map.get("comment");
            script.setComment(comment);
        }
        return mapper.updateByPrimaryKeySelective(script);
    }

    @Override
    public CommonResult debugScript(int scriptId) {
        EmergencyScript emergencyScript = mapper.selectByPrimaryKey(scriptId);
        if (emergencyScript == null) {
            return CommonResult.failed("请选择正确的脚本");
        }
        return execService.exec(emergencyScript);
    }

    @Override
    public LogRespone debugLog(int detailId, int lineIndex) {
        return execService.getLog(detailId,lineIndex);
    }

    private void extracted(EmergencyScript script) {
        transLateScript(script);
        try {
            if (script.getPasswordMode() != null && script.getPasswordMode().equals(TYPE_ZERO)) {
                script.setPassword(passwordUtil.encodePassword(script.getPassword()));
            }
        } catch (UnsupportedEncodingException e) {
            throw new ApiException("Failed to encode password. ", e);
        }
        script.setScriptStatus(TYPE_ZERO);
    }


    private boolean isParamInvalid(EmergencyScript script) {
        if (script.getHavePassword().equals("havePassword") &&
                (StringUtils.isBlank(script.getPassword()) || StringUtils.isBlank(script.getPasswordMode()))) {
            return true;
        }
        return false;
    }

    private void transLateScript(EmergencyScript script) {
        switch (script.getIsPublic()) {
            case PRIVATE:
                script.setIsPublic(TYPE_ZERO);
                break;
            case PUBLIC:
                script.setIsPublic(TYPE_ONE);
        }
        switch (script.getScriptType()) {
            case "Shell":
                script.setScriptType(TYPE_ZERO);
                break;
            case "Jython":
                script.setScriptType(TYPE_ONE);
                break;
            case "Groovy":
                script.setScriptType(TYPE_TWO);
        }
        switch (script.getHavePassword()) {
            case NO_PASSWORD:
                script.setHavePassword(TYPE_ZERO);
                break;
            case HAVE_PASSWORD:
                script.setHavePassword(TYPE_ONE);
        }
        if (script.getPasswordMode() != null) {
            switch (script.getPasswordMode()) {
                case "本地":
                    script.setPasswordMode(TYPE_ZERO);
                    break;
                case "平台":
                    script.setPasswordMode(TYPE_ONE);
            }
        }
    }
}

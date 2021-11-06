package com.huawei.emergency.service.impl;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.github.pagehelper.PageHelper;
import com.huawei.common.api.CommonResult;
import com.huawei.common.constant.ResultCode;
import com.huawei.common.exception.ApiException;
import com.huawei.common.util.FileUtil;
import com.huawei.common.util.PageUtil;
import com.huawei.emergency.dto.SearchScriptDto;
import com.huawei.emergency.entity.EmergencyScript;
import com.huawei.emergency.entity.EmergencyScriptExample;
import com.huawei.emergency.entity.User;
import com.huawei.emergency.mapper.EmergencyScriptMapper;
import com.huawei.emergency.service.EmergencyScriptService;
import com.huawei.emergency.service.UserAdminCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
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

    @Value("${key}")
    private String key;

    @Autowired
    private EmergencyScriptMapper mapper;

    @Override
    public CommonResult<List<EmergencyScript>> listScript(HttpServletRequest request, String scriptName, String scriptUser, int pageSize, int current, String sorter, String order) {
        if (order.equals("ascend")) {
            PageHelper.orderBy(sorter + System.lineSeparator() + "ASC");
        } else {
            PageHelper.orderBy(sorter + System.lineSeparator() + "DESC");
        }
        // todo
        /*User user = UserAdminCache.userMap.get(request.getHeader("token"));
        */
        //String auth = user.getAuth().contains("admin") ? "admin" : "";
        String userName = "30009881";
        String auth = "admin";
        List<EmergencyScript> emergencyScripts = mapper.listScript(userName, auth, scriptName, scriptUser);
        return CommonResult.success(PageUtil.startPage(emergencyScripts, current, pageSize), emergencyScripts.size());
    }

    @Override
    public int deleteScripts(Object[] data) {
        int count = 0;
        for (Object obj : data) {
            count += mapper.deleteByPrimaryKey((int) obj);
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
    public Map<String, String> uploadScript(MultipartFile file) {
        HashMap<String, String> map = null;
        try {
            InputStream inputStream = file.getInputStream();
            String content = FileUtil.streamToString(inputStream);
            String fileName = file.getResource().getFilename();
            map = new HashMap<>();
            map.put("content", content);
            map.put("script_name", fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public EmergencyScript selectScript(int scriptId) {
        EmergencyScript scriptInfo = mapper.getScriptInfo(scriptId);
        try {
            if(scriptInfo.getPasswordMode().equals("local")){
                scriptInfo.setPassword(decodePassword(scriptInfo.getPassword()));
            }
        } catch (UnsupportedEncodingException e) {
            throw new ApiException("Failed to decode password. ", e);
        }
        return scriptInfo;
    }

    @Override
    public int insertScript(HttpServletRequest request,EmergencyScript script) {
        if (isParamInvalid(script)) {
            return ResultCode.PARAM_INVALID;
        }
        EmergencyScriptExample example = new EmergencyScriptExample();
        example.createCriteria().andScriptNameEqualTo(script.getScriptName());
        long count = mapper.countByExample(example);
        if(count > 0){
            return ResultCode.SCRIPT_NAME_EXISTS;
        }
        extracted(request,script);
        return mapper.insertSelective(script);
    }

    @Override
    public int updateScript(HttpServletRequest request,EmergencyScript script) {
        if (isParamInvalid(script)) {
            return ResultCode.PARAM_INVALID;
        }

        // 脚本名是否修改了
        String oldScriptName = mapper.selectScriptNameById(script.getScriptId());
        String scriptName = script.getScriptName();
        if(!oldScriptName.equals(scriptName)){
            EmergencyScriptExample example = new EmergencyScriptExample();
            example.createCriteria().andScriptNameEqualTo(scriptName);
            long count = mapper.countByExample(example);
            if(count > 0){
                return ResultCode.SCRIPT_NAME_EXISTS;
            }
        }
        extracted(request,script);
        return mapper.updateByPrimaryKeySelective(script);
    }

    @Override
    public List<String> searchScript(HttpServletRequest request,String scriptName) {
        // todo
        String auth = "admin";
        String userName = "30009881";
        /*User user = UserAdminCache.userMap.get(request.getHeader("token"));
        String auth = user.getAuth().contains("admin") ? "admin" : "";*/
        List<String> list = mapper.searchScript(scriptName,userName,auth);
        return list;
    }

    @Override
    public EmergencyScript getScriptByName(String scriptName) {
        EmergencyScript scriptInfo = mapper.getScriptByName(scriptName);
        try {
            if(scriptInfo.getPasswordMode().equals("local")){
                scriptInfo.setPassword(decodePassword(scriptInfo.getPassword()));
            }
        } catch (UnsupportedEncodingException e) {
            throw new ApiException("Failed to decode password. ", e);
        }
        return scriptInfo;
    }

    private void extracted(HttpServletRequest request,EmergencyScript script) {
        transLateScript(script);
        try {
            if (script.getPasswordMode().equals("local")) {
                script.setPassword(encodePassword(script.getPassword()));
            }
        } catch (UnsupportedEncodingException e) {
            throw new ApiException("Failed to encode password. ", e);
        }
        // todo
        script.setScriptUser("admin");
        //script.setScriptUser(UserAdminCache.userMap.get(request.getHeader("token")).getUserName());
        script.setScriptStatus("0");
    }


    private boolean isParamInvalid(EmergencyScript script) {
        if (script.getHavePassword().equals("havePassword") && StringUtils.isBlank(script.getPassword())) {
            return true;
        }
        if (script.getHaveParam().equals("haveParam") && StringUtils.isBlank(script.getParam())) {
            return true;
        }
        return false;
    }

    private String encodePassword(String password) throws UnsupportedEncodingException {
        AES aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));

        // AES加密
        byte[] encrypt = aes.encrypt(password);

        // Base64加密
        byte[] encode = Base64.getEncoder().encode(encrypt);
        return new String(encode, "utf-8");
    }

    private String decodePassword(String password) throws UnsupportedEncodingException {
        AES aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));
        byte[] decode = Base64.getDecoder().decode(password);
        byte[] decrypt = aes.decrypt(decode);
        return new String(decrypt, "utf-8");
    }

    private void transLateScript(EmergencyScript script) {
        switch (script.getIsPublic()) {
            case "private":
                script.setIsPublic(TYPE_ZERO);
            case "public":
                script.setIsPublic(TYPE_ONE);
        }
        switch (script.getScriptType()) {
            case "shell":
                script.setScriptType(TYPE_ZERO);
            case "jython":
                script.setScriptType(TYPE_ONE);
            case "groovy":
                script.setScriptType(TYPE_TWO);
        }
        switch (script.getHavePassword()) {
            case "noPassword":
                script.setHavePassword(TYPE_ZERO);
            case "havePassword":
                script.setHavePassword(TYPE_ONE);
        }
        switch (script.getPasswordMode()) {
            case "local":
                script.setPasswordMode(TYPE_ZERO);
            case "platform":
                script.setPasswordMode(TYPE_ONE);
        }
        switch (script.getHaveParam()) {
            case "noParam":
                script.setHaveParam(TYPE_ZERO);
            case "haveParam":
                script.setHaveParam(TYPE_ONE);
        }
    }
}

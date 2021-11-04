package com.huawei.emergency.service.impl;

import com.github.pagehelper.PageHelper;
import com.huawei.common.api.CommonResult;
import com.huawei.common.exception.ApiException;
import com.huawei.common.util.FileUtil;
import com.huawei.common.util.PageUtil;
import com.huawei.emergency.entity.EmergencyScript;
import com.huawei.emergency.entity.EmergencyScriptExample;
import com.huawei.emergency.entity.User;
import com.huawei.emergency.mapper.EmergencyScriptMapper;
import com.huawei.emergency.service.EmergencyScriptService;
import com.huawei.emergency.service.UserAdminCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class EmergencyScriptServiceImpl implements EmergencyScriptService {
    private static final int BUF_SIZE = 1024;

    @Autowired
    private EmergencyScriptMapper mapper;

    @Override
    public CommonResult<List<EmergencyScript>> listScript(HttpServletRequest request, String scriptName, String scriptUser, int pageSize, int current, String sorter, String order) {
        if (order.equals("ascend")) {
            PageHelper.orderBy(sorter + System.lineSeparator() + "ASC");
        } else {
            PageHelper.orderBy(sorter + System.lineSeparator() + "DESC");
        }
        User user = UserAdminCache.userMap.get(request.getHeader("token"));
        String auth = user.getAuth().contains("admin") ? "admin" : "";
        List<EmergencyScript> emergencyScripts = mapper.listScript(user.getUserName(), auth, scriptName, scriptUser);
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
    public Map<String,String> uploadScript(MultipartFile file) {
        HashMap<String, String> map = null;
        try {
            InputStream inputStream = file.getInputStream();
            String content = FileUtil.streamToString(inputStream);
            String fileName = file.getResource().getFilename();
            map = new HashMap<>();
            map.put("content",content);
            map.put("script_name",fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public EmergencyScript selectScript(int scriptId) {
        return null;
    }
}

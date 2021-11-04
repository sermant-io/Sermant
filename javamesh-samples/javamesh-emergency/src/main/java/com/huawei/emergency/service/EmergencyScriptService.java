package com.huawei.emergency.service;

import com.huawei.common.api.CommonResult;
import com.huawei.emergency.entity.EmergencyScript;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface EmergencyScriptService {
    CommonResult<List<EmergencyScript>> listScript(HttpServletRequest request,String scriptName, String scriptUser, int pageSize, int current, String sorter, String order);

    int deleteScripts(Object[] data);

    void downloadScript(int scriptId, HttpServletResponse response);

    Map<String,String> uploadScript(MultipartFile file);

    EmergencyScript selectScript(int scriptId);
}

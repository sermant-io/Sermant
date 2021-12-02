package com.huawei.hercules.service.script;

import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.config.FeignRequestInterceptor;
import com.huawei.hercules.fallback.ScriptServiceFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        url = "${controller.engine.url}" + "/rest/script",
        name = "script",
        fallbackFactory = ScriptServiceFallbackFactory.class,
        configuration = FeignRequestInterceptor.class
)
public interface IScriptService {

    /**
     * 新建脚本
     *
     * @param path 路径
     * @param testUrl 目标地址
     * @param fileName 脚本名称
     * @param scriptType 脚本类型
     * @param createLibAndResources 是否创建资源文件
     * @param options 高级配置信息
     * @return 新建结果
     */
    @RequestMapping(value = "/new/script", method = RequestMethod.POST)
    JSONObject createForm(@RequestParam(required = false) String path,
                                 @RequestParam(value = "testUrl", required = false) String testUrl,
                                 @RequestParam("fileName") String fileName,
                                 @RequestParam(value = "scriptType", required = false) String scriptType,
                                 @RequestParam(value = "createLibAndResource", defaultValue = "false") boolean createLibAndResources,
                                 @RequestParam(value = "options", required = false) String options);


    /**
     * 保存脚本
     *
     * @param jsonObject 脚本信息
     * @param targetHosts 目标主机
     * @param validated 版本，默认传0
     * @param createLibAndResource 是否创建资源文件
     * @return 保存结果
     */
    @RequestMapping(value = "/saveScript", method = RequestMethod.POST)
    String saveScript(@RequestParam Map<String, String> jsonObject,
                      @RequestParam String targetHosts, @RequestParam(defaultValue = "0") String validated,
                      @RequestParam(defaultValue = "false") boolean createLibAndResource);

    /**
     * 按条件查询脚本
     *
     * @param query 关键字
     * @return 查询脚本结果
     */
    @RequestMapping(value = "/search")
    JSONObject search(@RequestParam(required = false) String query);

    /**
     * 查询脚本列表【暂时未分页，全部返回】
     *
     * @param path 路径
     * @return 查询脚本结果
     */
    @RequestMapping({"/list"})
    JSONObject getAllList(@RequestParam(required = false) String path);

    /**
     * 新建文件夹
     *
     * @param path 路径
     * @param folderName 文件夹名称
     * @return 创建结果
     */
    @RequestMapping(value = "/new/folder", method = RequestMethod.POST)
    String addFolder(@RequestParam(required = false) String path, @RequestParam("folderName") String folderName);

    /**
     * 删除脚本
     *
     * @param path 路径
     * @param filesString 文件名
     * @return 删除结果
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    String delete(@RequestParam(required = false) String path, @RequestParam("filesString") String filesString);

    /**
     * 下载脚本
     *
     * @param path 路径
     * @return 脚本信息
     */
    @RequestMapping("/downloadFile")
    JSONObject downloadFile(@RequestParam(required = false) String path);

    /**
     * 查看脚本
     *
     * @param path 路径
     * @param revision 版本
     * @return 脚本信息
     */
    @RequestMapping("/detail")
    JSONObject getOne(@RequestParam(required = false) String path,
                      @RequestParam(value = "r", required = false) Long revision);


    /**
     * 脚本验证
     *
     * @param jsonObject 脚本信息
     * @param hostString 主机信息
     * @return 验证结果
     */
    @RequestMapping(value = "/api/validateScript", method = RequestMethod.POST)
    HttpEntity<String> validateScript(@RequestParam String jsonObject,
                                      @RequestParam(value = "hostString", required = false) String hostString);

    /**
     * 判断脚本是否存在
     * @param path 脚本路径
     * @return 判断结果
     */
    @RequestMapping(value = "/hasScript", method = RequestMethod.GET)
    boolean hasScript(@RequestParam String path);
}

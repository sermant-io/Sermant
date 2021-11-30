package com.huawei.hercules.controller.scenario;

import com.alibaba.fastjson.JSONObject;
import com.huawei.hercules.controller.BaseController;
import com.huawei.hercules.service.scenario.IScenarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ScenarioController extends BaseController {

    @Autowired
    private IScenarioService scenarioService;

    /**
     * key的对应关系
     **/
    private static final Map<String, String> scenarioKeys;

    static {
        scenarioKeys = new HashMap<>();
        getKeys();
    }

    /**
     * 压测场景创建
     *
     * @param params 入参
     * @return 创建状态
     */
    @RequestMapping(value = "/scenario", method = RequestMethod.POST)
    public JSONObject create(@RequestBody JSONObject params) {
        if (StringUtils.isEmpty(params) || !params.containsKey("scenario_name") && !params.containsKey("app_name")) {
            return returnError("压测场景信息缺失不完善");
        }

        if (!StringUtils.isEmpty(params.getString("scenario_id"))) {
            // 更新压测场景
            params.put("id", params.getString("scenario_id"));
        } else {
            // 新建压测场景：压测场景只能有一个
            String scenarioName = params.getString("scenario_name");
            JSONObject result = scenarioService.getPagedAll(null, null, null, null, scenarioName, null);
            if (result != null) {
                Map<String, Object> scenarioListPage = (Map<String, Object>) result.get("scenarioListPage");
                if (scenarioListPage.get("total") != null && Integer.parseInt(scenarioListPage.get("total").toString()) > 0) {
                    return returnError("压测场景已存在");
                }
            }
        }

        List<String> labels = (List<String>) params.get("label");
        params.remove("label");
        params.put("label", arrayToStr(labels));

        params.put("scenario_type", CUSTOM_SCRIPT);
        scenarioService.create(params.toString());
        return returnSuccess();
    }

    @RequestMapping(value = "/scenario", method = RequestMethod.PUT)
    public JSONObject update(@RequestBody JSONObject params) {
        return create(params);
    }

    @RequestMapping(value = "/scenario", method = RequestMethod.GET)
    public JSONObject queryScenario(@RequestParam(required = false) String keywords,
                                    @RequestParam(name = "app_name[]", required = false) String[] app_name,
                                    @RequestParam(name = "create_by[]", required = false) String[] create_by,
                                    @RequestParam(name = "scenario_type[]", required = false) String[] scenario_type,
                                    @RequestParam(name = "scenario_name[]", required = false) String[] scenario_name,
                                    @RequestParam(required = false, defaultValue = "10") int pageSize,
                                    @RequestParam(required = false, defaultValue = "1") int current,
                                    @RequestParam(required = false) String sorter,
                                    @RequestParam(required = false) String order) {
        // 1.查询条件的转换
        JSONObject pagesInfo = new JSONObject();
        pagesInfo.put("size", pageSize);
        pagesInfo.put("page", current == 0 ? 0 : current - 1);
        if (!StringUtils.isEmpty(sorter) && !StringUtils.isEmpty(order)) {
            StringJoiner sj = new StringJoiner(",");
            sj.add(scenarioKeys.get(sorter)).add(getOrder(order));
            pagesInfo.put("sort", sj.toString());
        }

        JSONObject result = scenarioService.getPagedAll(keywords, arrayToStr(app_name), arrayToStr(create_by), arrayToStr(scenario_type), arrayToStr(scenario_name), pagesInfo.toString());

        // 2.结果适配
        List<Map<String, Object>> files = null;
        if (result != null) {
            Map<String, Object> scenarioListPage = (Map<String, Object>) result.get("scenarioListPage");
            files = (List<Map<String, Object>>) scenarioListPage.get("content");
            result.put("total", scenarioListPage.get("total"));
            for (Map<String, Object> file : files) {
                Set<Map.Entry<String, String>> entries = scenarioKeys.entrySet();
                for (Map.Entry<String, String> next : entries) {
                    file.put(next.getKey(), file.get(next.getValue()));
                }
                List<String> labels = new ArrayList<>();
                String label = (String) file.get("label");
                if (!StringUtils.isEmpty(label)) {
                    labels.addAll(Arrays.asList(label.trim().split(",")));
                }
                file.put("label", labels);

                Map<String, Object> user = (Map<String, Object>) file.get("createBy");
                String createBy = user != null ? (String) user.get("name") : null;
                file.put("create_by", createBy);
                scenarioKeys.put("create_by", "name");

                // 格式化日期格式
                file.put("create_time", dataFormat((String)file.get("create_time")));
                file.put("update_time", dataFormat((String)file.get("update_time")));
            }
        }
        result.put("data", files);
        return result;
    }

    @RequestMapping(value = "/scenario", method = RequestMethod.DELETE)
    public JSONObject delete(@RequestParam(name = "scenario_id[]") String[] scenario_id) {
        if (StringUtils.isEmpty(scenario_id)) {
            return returnError();
        }
        try {
            scenarioService.delete(arrayToStr(scenario_id));
        } catch (Exception e) {
            return returnError("压测场景删除失败");
        }
        return returnSuccess();
    }


    /**
     * 下拉列表查询
     *
     * @param value 关键字
     * @return 场景列表
     */
    @RequestMapping(value = {"/scenario/search"}, method = RequestMethod.GET)
    public JSONObject search(@RequestParam(required = false) String value) {
        JSONObject pagesInfo = new JSONObject();
        pagesInfo.put("size", Integer.MAX_VALUE);
        pagesInfo.put("page", 0);
        // 只查询脚本场景
        JSONObject result = scenarioService.getPagedAll(value, "", "", CUSTOM_SCRIPT, "", pagesInfo.toString());

        List<String> scenarioInfos = new ArrayList<>();
        if (result != null) {
            Map<String, Object> scenarioListPage = (Map<String, Object>) result.get("scenarioListPage");
            List<Map<String, Object>> content = (List<Map<String, Object>>) scenarioListPage.get("content");

            for (Map file : content) {
                scenarioInfos.add((String) file.get("scenarioName"));
            }
        }
        result.put("data", scenarioInfos);
        return result;
    }

    private static void getKeys() {
        if (scenarioKeys.isEmpty()) {
            scenarioKeys.put("scenario_id", "id");
            scenarioKeys.put("app_name", "appName");
            scenarioKeys.put("scenario_name", "scenarioName");
            scenarioKeys.put("scenario_type", "scenarioType");
            scenarioKeys.put("script_path", "scriptPath");
            scenarioKeys.put("create_time", "createTime");
            scenarioKeys.put("update_time", "updateTime");
        }
    }
}

package com.huawei.flowcontrol.console.datasource.entity.rule.kie.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator.RuleKiePublisher;
import com.huawei.flowcontrol.console.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.huawei.flowcontrol.console.util.SystemUtils.ERROR_CODE;

public class KieRuleCommonController<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(KieRuleCommonController.class);

    private final RuleKiePublisher<List<T>> rulePublisher;

    protected KieRuleCommonController(RuleKiePublisher<List<T>> rulePublisher) {
        this.rulePublisher = rulePublisher;
    }

    protected Result<String> apiDeleteRule(HttpServletRequest httpServletRequest, long id, String extInfo, String app) {
        JSONObject jsonObject = JSON.parseObject(extInfo);
        Object ruleIdObj = jsonObject.get("ruleId");
        if (ruleIdObj == null) {
            LOGGER.error("No ruleId in extInfo.");
            return Result.ofFail(ERROR_CODE, "Delete rule error.");
        }
        String ruleId = ruleIdObj.toString();
        try {
            rulePublisher.delete(app, ruleId);
            return Result.ofSuccess(ruleId);
        } catch (Throwable throwable) {
            LOGGER.error("Error when delete rules", throwable);
            return Result.ofThrowable(ERROR_CODE, throwable);
        }
    }
}

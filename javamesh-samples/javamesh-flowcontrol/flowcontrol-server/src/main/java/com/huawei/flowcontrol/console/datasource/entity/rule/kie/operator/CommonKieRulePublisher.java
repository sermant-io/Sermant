package com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.exception.KieGeneralException;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.KieConfigClient;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.response.KieConfigLabel;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.client.response.KieConfigResponse;
import com.huawei.flowcontrol.console.datasource.entity.rule.kie.util.KieConfig;
import com.huawei.flowcontrol.console.entity.BaseRule;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

import static com.huawei.flowcontrol.console.datasource.entity.rule.kie.operator.RuleKieProvider.MAX_LENGTH_OF_KIE_LABEL;

@Component
public class CommonKieRulePublisher<E extends AbstractRule, T extends BaseRule<E>> implements RuleKiePublisher<List<T>> {
    @Autowired
    KieConfigClient kieConfigClient;

    @Autowired
    KieConfig kieConfig;

    @Override
    public void update(String app, List<T> entities) throws Exception {
        if (StringUtils.isEmpty(app) || CollectionUtils.isEmpty(entities)) {
            throw new IllegalArgumentException("app or entities empty.");
        }
        String urlPrefix = kieConfig.getKieBaseUrl();
        entities.forEach(ruleEntity -> {
            String url = urlPrefix + "/" + getRuleId(ruleEntity);
            AbstractRule rule = ruleEntity.toRule();
            Optional<KieConfigResponse> response = kieConfigClient.updateConfig(url, rule);
            if (!response.isPresent()) {
                RecordLog.error("Update rules failed");
                throw new KieGeneralException("Update rules failed");
            }
        });
    }

    @Override
    public void add(String app, List<T> entities) {
        if (StringUtils.isEmpty(app) || CollectionUtils.isEmpty(entities)) {
            throw new IllegalArgumentException("app or entities empty.");
        }

        KieConfigLabel kieConfigLabel = KieConfigLabel.builder()
            .service(app)
            .build();

        String url = kieConfig.getKieBaseUrl();
        entities.forEach(ruleEntity -> {
            String resourceStr = ruleEntity.getResource().replace("/", "")
                .replace(":", "");
            if (resourceStr.length() > MAX_LENGTH_OF_KIE_LABEL) {
                resourceStr = resourceStr.substring(resourceStr.length() - MAX_LENGTH_OF_KIE_LABEL);
            }
            kieConfigLabel.setResource(resourceStr);
            AbstractRule rule = ruleEntity.toRule();
            Optional<KieConfigResponse> response = kieConfigClient.addConfig(url, "rule", rule,
                kieConfigLabel);
            if (!response.isPresent()) {
                RecordLog.error("Add rules failed");
                throw new KieGeneralException("Add rules failed");
            }
        });
    }

    @Override
    public void delete(String app, String ruleId) {
        if (StringUtils.isEmpty(app) || StringUtils.isEmpty(ruleId)) {
            throw new IllegalArgumentException("app or ruleId empty.");
        }
        String url = kieConfig.getKieBaseUrl();
        kieConfigClient.deleteConfig(url, ruleId);
    }

    public String getRuleId(T entity) {
        JSONObject ruleExtInfo = JSON.parseObject(entity.getExtInfo());
        if (!ruleExtInfo.containsKey("ruleId")) {
            String errorMessage = "Cannot find ruleId from ruleExtInfo";
            RecordLog.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        return ruleExtInfo.getString("ruleId");
    }
}

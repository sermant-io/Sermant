package com.huawei.flowcontrol.console.entity;

import com.alibaba.csp.sentinel.slots.system.SystemRule;
import lombok.Getter;
import lombok.Setter;

/**
 * 此处部分引用alibaba/Sentinel开源社区代码，诚挚感谢alibaba/Sentinel开源团队的慷慨贡献
 */
@Getter
@Setter
public class SystemRuleVo extends BaseRule<SystemRule> {
    private Double highestSystemLoad;
    private Long avgRt;
    private Long maxThread;
    private Double qps;
    private Double highestCpuUsage;

    public static SystemRuleVo fromSystemRule(String app, String ip, Integer port, SystemRule rule) {
        SystemRuleVo entity = new SystemRuleVo();
        entity.setApp(app);
        entity.setIp(ip);
        entity.setPort(port);
        entity.setHighestSystemLoad(rule.getHighestSystemLoad());
        entity.setHighestCpuUsage(rule.getHighestCpuUsage());
        entity.setAvgRt(rule.getAvgRt());
        entity.setMaxThread(rule.getMaxThread());
        entity.setQps(rule.getQps());
        return entity;
    }

    @Override
    public SystemRule toRule() {
        SystemRule rule = new SystemRule();
        rule.setHighestSystemLoad(highestSystemLoad);
        rule.setAvgRt(avgRt);
        rule.setMaxThread(maxThread);
        rule.setQps(qps);
        rule.setHighestCpuUsage(highestCpuUsage);
        return rule;
    }
}

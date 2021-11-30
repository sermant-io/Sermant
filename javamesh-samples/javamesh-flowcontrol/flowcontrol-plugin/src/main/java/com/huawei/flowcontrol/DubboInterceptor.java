package com.huawei.flowcontrol;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.huawei.apm.core.agent.common.BeforeResult;
import com.huawei.apm.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.flowcontrol.exception.FlowControlException;
import com.huawei.flowcontrol.entry.EntryFacade;
import com.huawei.flowcontrol.util.SentinelRuleUtil;

import java.util.Locale;

public abstract class DubboInterceptor implements InstanceMethodInterceptor {

    protected String getResourceName(String interfaceName, String methodName) {
        return interfaceName + ":" + methodName;
    }

    protected void handleBlockException(BlockException ex, String resourceName, BeforeResult result, String type, EntryFacade.DubboType dubboType) {
        try {
            final String msg = String.format(Locale.ENGLISH,
                "[%s] has been blocked! [appName=%s, resourceName=%s]",
                type, ex.getRuleLimitApp(), resourceName);
            RecordLog.info(msg);
            String res = SentinelRuleUtil.getResult(ex.getRule());
            result.setResult(res);
            throw new FlowControlException(res);
        } finally {
            if (EntryFacade.DubboType.APACHE == dubboType) {
                EntryFacade.INSTANCE.exit(dubboType);
            } else {
                EntryFacade.INSTANCE.exit(dubboType);
            }
        }
    }
}

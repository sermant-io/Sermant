/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Based on com/alibaba/csp/sentinel/slots/block/flow/FlowSlot.java
 * from the Alibaba Sentinel project.
 */

package com.huawei.flowcontrol.adapte.cse.rule.isolate;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.spi.SpiOrder;
import com.alibaba.csp.sentinel.util.function.BiConsumer;

import java.util.List;

/**
 * 隔离仓实现 拦截优先级如下: 流控 > 隔离仓 > 熔断
 *
 * @author zhouss
 * @since 2021-12-04
 */
@SpiOrder(-1500)
public class IsolateThreadSlot extends AbstractLinkedProcessorSlot<DefaultNode> {
    @Override
    @SuppressWarnings("checkstyle:ParameterNumber")
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode param, final int count,
        boolean isPrioritized,
        Object... args) throws Throwable {
        fireEntry(context, resourceWrapper, param, count, isPrioritized, args);
        final List<IsolateThreadRule> rules = IsolateThreadRuleManager.getRules(resourceWrapper.getName());
        if (rules == null || rules.size() == 0) {
            return;
        }
        tryEntry(rules, count);
        bindEntryExitEvent(context.getCurEntry(), rules, count);
    }

    @Override
    @SuppressWarnings("checkstyle:RegexpSingleline")
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        fireExit(context, resourceWrapper, count, args);
    }

    private void tryEntry(final List<IsolateThreadRule> rules, int count) throws IsolateThreadException {
        for (IsolateThreadRule rule : rules) {
            rule.tryEntry(count);
        }
    }

    /**
     * 绑定entry退出事件， 使entry与exit绑定在一块，避免错误release许可，导致统计数据有误
     *
     * @param curEntry 当前Entry
     * @param rules 匹配规则列表
     * @param count 许可数
     */
    private void bindEntryExitEvent(final Entry curEntry, final List<IsolateThreadRule> rules, final int count) {
        curEntry.whenTerminate(new IsolateEntryConsumer(rules, count));
    }

    static class IsolateEntryConsumer implements BiConsumer<Context, Entry> {
        private final List<IsolateThreadRule> rules;
        private final int count;

        IsolateEntryConsumer(List<IsolateThreadRule> rules, int count) {
            this.rules = rules;
            this.count = count;
        }

        @Override
        public void accept(Context context, Entry entry) {
            if (entry.getBlockError() != null || rules == null) {
                return;
            }
            for (IsolateThreadRule rule : rules) {
                // 释放许可
                rule.exit(count);
            }
        }
    }
}

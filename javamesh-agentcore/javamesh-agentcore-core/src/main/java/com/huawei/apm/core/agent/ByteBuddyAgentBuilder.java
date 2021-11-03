package com.huawei.apm.core.agent;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.instrument.Instrumentation;

import com.huawei.apm.core.agent.transformer.DelegateTransformer;

/**
 * 插件增强Builder
 */
public class ByteBuddyAgentBuilder {
    public static void initialize(Instrumentation instrumentation) {
        new AgentBuilder.Default(new ByteBuddy())
            .ignore(new IgnoreClassMatcher())
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .type(EnhanceDefinitionLoader.getInstance().buildMatch())
            .transform(new DelegateTransformer())
            .with(new LoadListener())
            .installOn(instrumentation);
    }
}

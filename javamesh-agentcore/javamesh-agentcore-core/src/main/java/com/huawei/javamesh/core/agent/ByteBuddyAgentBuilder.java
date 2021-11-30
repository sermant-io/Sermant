package com.huawei.javamesh.core.agent;

import java.lang.instrument.Instrumentation;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;

import com.huawei.javamesh.core.agent.transformer.CommonTransformer;

/**
 * 插件增强Builder
 */
public class ByteBuddyAgentBuilder {
    public static void initialize(Instrumentation instrumentation) {
        new AgentBuilder.Default(new ByteBuddy())
            .ignore(new IgnoreClassMatcher())
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .type(EnhanceDefinitionLoader.getInstance().buildMatch())
            .transform(new CommonTransformer())
            .with(new LoadListener())
            .installOn(instrumentation);
    }
}

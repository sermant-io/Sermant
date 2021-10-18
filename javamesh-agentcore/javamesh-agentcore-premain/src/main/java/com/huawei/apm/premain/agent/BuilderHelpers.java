package com.huawei.apm.premain.agent;

import com.huawei.apm.premain.classloader.PluginClassLoader;
import com.huawei.apm.premain.enhance.enhancer.MemberFieldsHandler;
import com.huawei.apm.bootstrap.lubanops.AttributeAccess;
import com.huawei.apm.bootstrap.lubanops.TransformAccess;
import com.huawei.apm.premain.lubanops.classloader.LopsUrlClassLoader;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

import java.security.ProtectionDomain;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * byte buddy 构建工具类
 */
public class BuilderHelpers {

    private static final String ENHANCED_FIELD_NAME = "_$lopsAttribute_enhanced";

    public static DynamicType.Builder<?> addEnhancedField(DynamicType.Builder<?> newBuilder) {
        return newBuilder.defineField(ENHANCED_FIELD_NAME, Object.class, Opcodes.ACC_PRIVATE)
            .implement(TransformAccess.class)
            .intercept(FieldAccessor.ofField(ENHANCED_FIELD_NAME));
    }

    /**
     * 添加成员变量
     *
     * @param newBuilder 构建器
     * @param fields     定义的成员变量属性
     * @return 构建器
     */
    public static DynamicType.Builder<?> addListenerFields(DynamicType.Builder<?> newBuilder, List<String> fields) {
        if (fields == null || fields.size() == 0) {
            return newBuilder;
        }
        return newBuilder.implement(AttributeAccess.class)
            .method(named("getLopsFileds"))
            .intercept(MethodDelegation.withDefaultConfiguration().to(new MemberFieldsHandler(fields)));
    }

    /**
     * 排除增强对象
     *
     * @return true 排除
     * false 可增强对象
     */
    public static ElementMatcher.Junction<NamedElement> buildIgnoreClassNamePrefixMatch() {
        return nameStartsWith("com.lubanops.apm.bootstrap")
            .or(nameStartsWith("sun."))
            .or(nameStartsWith("org.w3c.dom."))
            .or(nameStartsWith("org.omg."))
            .or(nameStartsWith("com.sun."))
            .or(nameStartsWith("java."))
            .or(nameStartsWith("javax."))
            .or(nameStartsWith("Lambda"))
            .or(nameStartsWith("com.lubanops.apm.plugin"))
            .or(nameStartsWith("com.lubanops.apm.premain"))
            .or(nameStartsWith("org.apache.skywalking"))
            .or(nameStartsWith("com.huawei."))
            .or(nameStartsWith("net.bytebuddy"))
            .or(nameStartsWith("org.objectweb.asm"))
            .or(nameStartsWith("org.aspectj.weaver"))
            .or(nameStartsWith("com.alibaba.ttl"))
            .or(nameStartsWith("com.baidu.openrasp"))
            .or(nameStartsWith("org.javassist"));
    }

    /**
     * 过滤自身类加载器加载的类
     *
     * @return 自身类加载匹配器
     */
    public static AgentBuilder.RawMatcher ignoreAgentClassLoader() {
        return new AgentClassLoaderMatcher();
    }

    static class AgentClassLoaderMatcher implements AgentBuilder.RawMatcher {
        @Override
        public boolean matches(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, Class<?> classBeingRedefined, ProtectionDomain protectionDomain) {
            if (classLoader == null) {
                return false;
            }
            final String classLoaderName = classLoader.getClass().getName();
            return classLoaderName.equals(PluginClassLoader.class.getName())
                || classLoaderName.equals(LopsUrlClassLoader.class.getName());
        }
    }
}

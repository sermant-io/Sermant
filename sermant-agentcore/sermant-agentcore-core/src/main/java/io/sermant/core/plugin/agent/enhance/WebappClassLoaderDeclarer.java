package io.sermant.core.plugin.agent.enhance;

import io.sermant.core.plugin.agent.declarer.AbstractPluginDeclarer;
import io.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import io.sermant.core.plugin.agent.matcher.ClassMatcher;
import io.sermant.core.plugin.agent.matcher.MethodMatcher;

/**
 * 发行版Tomcat ClassLoader 增强
 *
 * @author Yaxx19
 * @since 2024-06-04
 */
public class WebappClassLoaderDeclarer extends AbstractPluginDeclarer {

    private static final String TOMCAT_CLASS_LOADER = "org.apache.catalina.loader.WebappClassLoaderBase";

    @Override
    public ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(TOMCAT_CLASS_LOADER);
    }

    @Override
    public InterceptDeclarer[] getInterceptDeclarers(ClassLoader classLoader) {
        return new InterceptDeclarer[] {
                InterceptDeclarer.build(MethodMatcher.nameEquals("loadClass"),
                        new ClassLoaderLoadClassInterceptor()),
                InterceptDeclarer.build(MethodMatcher.nameEquals("getResourceAsStream"),
                        new WebappClassLoaderGetResourceAsStreamInterceptor())};
    }
}

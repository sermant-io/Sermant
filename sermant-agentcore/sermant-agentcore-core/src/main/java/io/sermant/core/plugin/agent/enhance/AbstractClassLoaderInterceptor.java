package io.sermant.core.plugin.agent.enhance;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.agent.interceptor.Interceptor;
import io.sermant.core.service.inject.config.InjectConfig;

import java.util.Set;

/**
 * ClassLoader 增强抽象类
 *
 * @author Yaxx19
 * @since 2024-06-04
 */
public abstract class AbstractClassLoaderInterceptor implements Interceptor {

    private final Set<String> essentialPackage;

    /**
     * constructor
     */
    public AbstractClassLoaderInterceptor() {
        essentialPackage = ConfigManager.getConfig(InjectConfig.class).getEssentialPackage();
    }

    protected boolean isSermantClass(String name) {
        for (String prefix : essentialPackage) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isSermantResource(String path) {
        String name = path.replace('/', '.');
        for (String prefix : essentialPackage) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}

package io.sermant.core.plugin.agent.enhance;

import io.sermant.core.classloader.ClassLoaderManager;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.agent.entity.ExecuteContext;

import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 发行版Tomcat ClassLoader getResourceAsStream 增强
 *
 * @author Yaxx19
 * @since 2024-06-04
 */
public class WebappClassLoaderGetResourceAsStreamInterceptor extends AbstractClassLoaderInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        if (context.getResult() != null) {
            return context;
        }

        String path = (String) context.getArguments()[0];
        if (isSermantResource(path)) {
            Optional<URL> url = ClassLoaderManager.getPluginClassFinder().findSermantResource(path);
            if (!url.isPresent()) {
                LOGGER.log(Level.WARNING, "Can not get resource stream [{0}] by sermant.And then find by {1}. ",
                        new Object[]{path, context.getObject()});
            } else {
                context.changeResult(url.get().openStream());
                LOGGER.log(Level.INFO, "Get resource stream: {0} successfully by sermant.", path);
            }
        }
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        return context;
    }
}

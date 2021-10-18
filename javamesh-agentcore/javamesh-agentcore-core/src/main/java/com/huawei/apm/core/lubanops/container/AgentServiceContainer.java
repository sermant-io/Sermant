package com.huawei.apm.core.lubanops.container;

import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.huawei.apm.bootstrap.lubanops.api.Container;
import com.huawei.apm.bootstrap.lubanops.holder.AgentServiceContainerHolder;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.bootstrap.lubanops.trace.TraceCollector;
import com.huawei.apm.bootstrap.lubanops.trace.TraceReportService;
import com.huawei.apm.core.lubanops.api.AgentService;
import com.huawei.apm.core.lubanops.container.module.AbstractAgentModule;
import com.huawei.apm.core.lubanops.utils.ClassLoaderUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Agent Service Container.<p>
 * This service has some management functions,as shown below.
 * <li>create and registry service instance to guice context
 * <li>initialize agent services
 * <li>stops agent services
 *
 * @author
 * @date 2020/10/15 21:03
 */
public class AgentServiceContainer implements Container {

    private static final Logger LOGGER = LogFactory.getLogger();

    private final List<AgentService> agentServiceList = new ArrayList<AgentService>();

    /**
     * Synchronization monitor
     */
    private final Object startupShutdownMonitor = new Object();

    /**
     * Activity flag
     */
    private final AtomicBoolean active = new AtomicBoolean(false);

    /**
     * Container stopped flag
     */
    private final AtomicBoolean closed = new AtomicBoolean(true);

    private Injector injector;

    private long startupTime;

    @Override
    public void start() {
        synchronized (this.startupShutdownMonitor) {
            this.startupTime = System.currentTimeMillis();
            this.closed.set(false);
            this.active.set(true);
            ClassLoader preClassLoader = ClassLoaderUtils.pushContextClassLoader(getClass().getClassLoader());
            try {
                LOGGER.info("begin to start AgentServiceContainer");
                injector = Guice.createInjector(Stage.PRODUCTION, findServiceModules());
                inject2BootstrapBean();
                AgentServiceContainerHolder.set(this);
                for (Binding<AgentService> binder : injector.findBindingsByType(new TypeLiteral<AgentService>() {
                })) {
                    agentServiceList.add(binder.getProvider().get());
                }
                Collections.sort(agentServiceList, new PriorityComparator());
                for (AgentService service : agentServiceList) {
                    service.init();
                    LOGGER.info(String.format("AgentService:%s has initialized", service.getClass().getSimpleName()));
                }
                LOGGER.info("finish to start AgentServiceContainer");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "start agent service container failed", e);
            } finally {
                ClassLoaderUtils.popContextClassLoader(preClassLoader);
            }
        }
    }

    @Override
    public void stop() {
        synchronized (this.startupShutdownMonitor) {
            this.closed.set(true);
            this.active.set(false);
            ClassLoader preClassLoader = ClassLoaderUtils.pushContextClassLoader(getClass().getClassLoader());
            try {
                LOGGER.info("begin to stop AgentServiceContainer");
                Collections.reverse(agentServiceList);
                for (AgentService service : agentServiceList) {
                    service.dispose();
                    LOGGER.info(String.format("AgentService:%s has disposed", service.getClass().getSimpleName()));
                }
                LOGGER.info("finish to stop AgentServiceContainer");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "stop agent service container failed", e);
            } finally {
                ClassLoaderUtils.popContextClassLoader(preClassLoader);
            }
        }
    }

    /**
     * Get service by type.
     *
     * @param clazz type
     * @param <T>   type
     * @return instance
     */
    @Override
    public <T> T getService(Class<T> clazz) {
        return this.injector.getInstance(clazz);
    }

    /**
     * started or not
     *
     * @return
     */
    public boolean isStarted() {
        return active.get();
    }

    /**
     * Running or not
     *
     * @return
     */
    public boolean isRunning() {
        return active.get() && !closed.get();
    }

    /**
     * Startup timestamp.
     *
     * @return
     */
    public long getStartupTime() {
        return this.startupTime;
    }

    //~~ inner method

    /**
     * Find service modules by service loader.
     *
     * @return
     */
    private List<AbstractAgentModule> findServiceModules() {
        List<AbstractAgentModule> modules = new ArrayList<AbstractAgentModule>();
        for (AbstractAgentModule abstractModule : ServiceLoader.load(AbstractAgentModule.class)) {
            modules.add(abstractModule);
        }
        return modules;
    }

    private void inject2BootstrapBean() {
        //inject internal service to bootstrap bean.
        TraceCollector.setReportService(getService(TraceReportService.class));
    }
}

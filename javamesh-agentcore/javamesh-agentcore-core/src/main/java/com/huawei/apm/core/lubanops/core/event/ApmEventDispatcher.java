package com.huawei.apm.core.lubanops.core.event;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.huawei.apm.core.lubanops.bootstrap.api.EventDispatcher;
import com.huawei.apm.core.lubanops.bootstrap.event.ApmEvent;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.lubanops.core.executor.ExecuteRepository;

/**
 * @author
 * @date 2021/2/3 15:34
 */
public class ApmEventDispatcher implements EventDispatcher {
    private static final Logger LOGGER = LogFactory.getLogger();

    EventBus eventBus;

    AsyncEventBus asyncEventBus;

    @Inject
    public ApmEventDispatcher(ExecuteRepository executeRepository) {
        eventBus = new EventBus();
        asyncEventBus = new AsyncEventBus(executeRepository.getSharedExecutor());
    }

    @Override
    public void dispatch(ApmEvent event) {

        if (event == null || event.getEventType() == null) {
            LOGGER.log(Level.SEVERE, "[EVENT DISPATCHER]empty event object.");
        }
        eventBus.post(event);
    }
}

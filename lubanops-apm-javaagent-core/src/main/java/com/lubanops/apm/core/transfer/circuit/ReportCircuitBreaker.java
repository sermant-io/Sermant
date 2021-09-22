package com.lubanops.apm.core.transfer.circuit;

import com.google.inject.Inject;
import com.lubanops.apm.bootstrap.api.CircuitBreaker;
import com.lubanops.apm.bootstrap.commons.LubanApmConstants;
import com.lubanops.apm.bootstrap.plugin.apm.APMCollector;
import com.lubanops.apm.core.executor.ExecuteRepository;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author
 * @date 2020/12/17 14:35
 */
public class ReportCircuitBreaker implements CircuitBreaker {

    public AtomicBoolean reset = new AtomicBoolean(true);

    public long resetTime = System.currentTimeMillis();

    @Inject
    ExecuteRepository executeRepository;

    private APMCollector apmCollector = APMCollector.INSTANCE;

    /**
     * open flag,default close.
     */
    private AtomicBoolean state = new AtomicBoolean(false);

    public ReportCircuitBreaker() {
    }

    @Override
    public boolean allowRequest() {
        if (isOpen()) {
            if (reset.get()) {
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isOpen() {
        return state.get();
    }

    @Override
    public void markSuccess() {
        if (state.get()) {
            state.compareAndSet(true, false);
        }
    }

    @Override
    public void markFailure() {
        if (!state.get()) {
            if (apmCollector.successPercent(LubanApmConstants.SPAN_EVENT_DATA_TYPE) > 0.5
                    && apmCollector.successPercent(LubanApmConstants.MONITOR_DATA_TYPE) > 0.5) {
                state.compareAndSet(false, true);
            }
        }
    }
}

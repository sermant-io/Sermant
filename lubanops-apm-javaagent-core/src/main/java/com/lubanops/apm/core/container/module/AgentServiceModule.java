package com.lubanops.apm.core.container.module;

import com.google.inject.multibindings.Multibinder;
import com.lubanops.apm.bootstrap.api.CircuitBreaker;
import com.lubanops.apm.bootstrap.api.EventDispatcher;
import com.lubanops.apm.bootstrap.trace.TraceReportService;
import com.lubanops.apm.core.api.AgentService;
import com.lubanops.apm.core.api.IntervalTaskManager;
import com.lubanops.apm.core.event.ApmEventDispatcher;
import com.lubanops.apm.core.executor.ExecuteRepository;
import com.lubanops.apm.core.executor.manager.DefaultExecuteRepository;
import com.lubanops.apm.core.master.MasterService;
import com.lubanops.apm.core.master.RegionMasterService;
import com.lubanops.apm.core.monitor.HarvestTaskManager;
import com.lubanops.apm.core.monitor.MonitorReportService;
import com.lubanops.apm.core.monitor.MonitorReportServiceImpl;
import com.lubanops.apm.core.trace.TraceReportServiceImpl;
import com.lubanops.apm.core.transfer.InvokerService;
import com.lubanops.apm.core.transfer.TransferInvokerService;
import com.lubanops.apm.core.transfer.circuit.ReportCircuitBreaker;

/**
 * Agent Service Module.
 * @author
 * @date 2020/10/20 17:52
 */
public class AgentServiceModule extends AbstractAgentModule {
    @Override
    protected void configure() {
        // declare agent services
        Multibinder<AgentService> agentServiceMultiBinder = Multibinder.newSetBinder(binder(), AgentService.class);
        agentServiceMultiBinder.addBinding().to(RegionMasterService.class);
        agentServiceMultiBinder.addBinding().to(MonitorReportServiceImpl.class);
        agentServiceMultiBinder.addBinding().to(TraceReportServiceImpl.class);
        agentServiceMultiBinder.addBinding().to(DefaultExecuteRepository.class);
        agentServiceMultiBinder.addBinding().to(DefaultExecuteRepository.class);
        agentServiceMultiBinder.addBinding().to(TransferInvokerService.class);

        // register implementation
        binder().bind(IntervalTaskManager.class).to(HarvestTaskManager.class);
        binder().bind(MonitorReportService.class).to(MonitorReportServiceImpl.class);
        binder().bind(TraceReportService.class).to(TraceReportServiceImpl.class);
        binder().bind(MasterService.class).to(RegionMasterService.class);
        binder().bind(InvokerService.class).to(TransferInvokerService.class);
        binder().bind(CircuitBreaker.class).to(ReportCircuitBreaker.class);
        requestInjection(new DefaultExecuteRepository());
        binder().bind(ExecuteRepository.class).toProvider(getProvider(DefaultExecuteRepository.class));
        binder().bind(EventDispatcher.class).to(ApmEventDispatcher.class);

    }
}

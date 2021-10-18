package com.huawei.apm.core.lubanops.container.module;

import com.google.inject.multibindings.Multibinder;
import com.huawei.apm.bootstrap.lubanops.api.CircuitBreaker;
import com.huawei.apm.bootstrap.lubanops.api.EventDispatcher;
import com.huawei.apm.bootstrap.lubanops.trace.TraceReportService;
import com.huawei.apm.core.lubanops.api.AgentService;
import com.huawei.apm.core.lubanops.api.IntervalTaskManager;
import com.huawei.apm.core.lubanops.event.ApmEventDispatcher;
import com.huawei.apm.core.lubanops.trace.TraceReportServiceImpl;
import com.huawei.apm.core.lubanops.transfer.circuit.ReportCircuitBreaker;
import com.huawei.apm.core.lubanops.executor.ExecuteRepository;
import com.huawei.apm.core.lubanops.executor.manager.DefaultExecuteRepository;
import com.huawei.apm.core.lubanops.master.MasterService;
import com.huawei.apm.core.lubanops.master.RegionMasterService;
import com.huawei.apm.core.lubanops.monitor.HarvestTaskManager;
import com.huawei.apm.core.lubanops.monitor.MonitorReportService;
import com.huawei.apm.core.lubanops.monitor.MonitorReportServiceImpl;
import com.huawei.apm.core.lubanops.transfer.InvokerService;
import com.huawei.apm.core.lubanops.transfer.TransferInvokerService;

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

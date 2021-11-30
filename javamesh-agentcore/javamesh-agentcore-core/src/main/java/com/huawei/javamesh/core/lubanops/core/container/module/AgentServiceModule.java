package com.huawei.javamesh.core.lubanops.core.container.module;

import com.google.inject.multibindings.Multibinder;
import com.huawei.javamesh.core.lubanops.bootstrap.api.CircuitBreaker;
import com.huawei.javamesh.core.lubanops.bootstrap.api.EventDispatcher;
import com.huawei.javamesh.core.lubanops.bootstrap.trace.TraceReportService;
import com.huawei.javamesh.core.lubanops.core.api.AgentService;
import com.huawei.javamesh.core.lubanops.core.api.IntervalTaskManager;
import com.huawei.javamesh.core.lubanops.core.event.ApmEventDispatcher;
import com.huawei.javamesh.core.lubanops.core.trace.TraceReportServiceImpl;
import com.huawei.javamesh.core.lubanops.core.transfer.circuit.ReportCircuitBreaker;
import com.huawei.javamesh.core.lubanops.core.executor.ExecuteRepository;
import com.huawei.javamesh.core.lubanops.core.executor.manager.DefaultExecuteRepository;
import com.huawei.javamesh.core.lubanops.core.master.MasterService;
import com.huawei.javamesh.core.lubanops.core.master.RegionMasterService;
import com.huawei.javamesh.core.lubanops.core.monitor.HarvestTaskManager;
import com.huawei.javamesh.core.lubanops.core.monitor.MonitorReportService;
import com.huawei.javamesh.core.lubanops.core.monitor.MonitorReportServiceImpl;
import com.huawei.javamesh.core.lubanops.core.transfer.InvokerService;
import com.huawei.javamesh.core.lubanops.core.transfer.TransferInvokerService;

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

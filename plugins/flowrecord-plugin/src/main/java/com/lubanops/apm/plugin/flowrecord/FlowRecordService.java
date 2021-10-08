package com.lubanops.apm.plugin.flowrecord;

import com.huawei.apm.bootstrap.boot.PluginService;
import lombok.SneakyThrows;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class FlowRecordService implements PluginService {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "FLOW_RECORD_INIT_THREAD");
        }
    });

    @Override
    public void init() {
        executorService.submit(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
            }
        });
    }

    @Override
    public void stop() {
        executorService.shutdown();
    }
}

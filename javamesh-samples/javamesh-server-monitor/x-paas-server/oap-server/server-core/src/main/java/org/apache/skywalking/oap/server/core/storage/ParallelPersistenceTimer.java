package org.apache.skywalking.oap.server.core.storage;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.util.RunnableWithExceptionProtection;
import org.apache.skywalking.oap.server.core.CoreModuleConfig;
import org.apache.skywalking.oap.server.core.analysis.worker.MetricsStreamProcessor;
import org.apache.skywalking.oap.server.core.analysis.worker.PersistenceWorker;
import org.apache.skywalking.oap.server.core.analysis.worker.TopNStreamProcessor;
import org.apache.skywalking.oap.server.library.client.request.PrepareRequest;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;
import org.apache.skywalking.oap.server.telemetry.TelemetryModule;
import org.apache.skywalking.oap.server.telemetry.api.CounterMetrics;
import org.apache.skywalking.oap.server.telemetry.api.HistogramMetrics;
import org.apache.skywalking.oap.server.telemetry.api.MetricsCreator;
import org.apache.skywalking.oap.server.telemetry.api.MetricsTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Huawei 并行指标分布式聚合定时器
 * @since 2021-05-10
 */
@Slf4j
public enum ParallelPersistenceTimer {
    INSTANCE;
    private Boolean isStarted = false;
    private final Boolean debug;
    private CounterMetrics errorCounter;
    private HistogramMetrics prepareLatency;
    private HistogramMetrics executeLatency;
    private long lastTime = System.currentTimeMillis();
    private final List<PrepareRequest> prepareRequests = Collections
        .synchronizedList(new LinkedList<>());


    private ExecutorService pool;

    ParallelPersistenceTimer() {
        this.debug = System.getProperty("debug") != null;
    }

    public void start(ModuleManager moduleManager, CoreModuleConfig moduleConfig) {
        log.info("parallel persistence timer start");
        IBatchDAO batchDAO = moduleManager.find(StorageModule.NAME).provider().getService(IBatchDAO.class);

        MetricsCreator metricsCreator = moduleManager.find(TelemetryModule.NAME)
            .provider()
            .getService(MetricsCreator.class);
        errorCounter = metricsCreator.createCounter(
            "parallel_persistence_timer_bulk_error_count", "Error execution of the prepare stage in persistence timer",
            MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE
        );
        prepareLatency = metricsCreator.createHistogramMetric(
            "parallel_persistence_timer_bulk_prepare_latency", "Latency of the prepare stage in persistence timer",
            MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE
        );
        executeLatency = metricsCreator.createHistogramMetric(
            "parallel_persistence_timer_bulk_execute_latency", "Latency of the execute stage in persistence timer",
            MetricsTag.EMPTY_KEY, MetricsTag.EMPTY_VALUE
        );

        pool = Executors.newFixedThreadPool(moduleConfig.getParallelThreadNum(),
            new ThreadFactory() {
                private final AtomicInteger seq = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "t-request-build-" + seq.getAndIncrement());
                }
            });

        if (!isStarted) {
            Executors.newSingleThreadScheduledExecutor()
                .scheduleWithFixedDelay(
                    new RunnableWithExceptionProtection(() -> extractDataAndSave(batchDAO), t -> log
                        .error("Extract data and save failure.", t)), 5, moduleConfig.getPersistentPeriod(),
                    TimeUnit.SECONDS
                );

            this.isStarted = true;
        }
    }

    private void extractDataAndSave(IBatchDAO batchDAO) {
        if (log.isDebugEnabled()) {
            log.debug("Extract data and save");
        }

        long startTime = System.currentTimeMillis();

        try {
            HistogramMetrics.Timer timer = prepareLatency.createTimer();

            try {
                List<PersistenceWorker<? extends StorageData>> persistenceWorkers = new ArrayList<>();
                persistenceWorkers.addAll(TopNStreamProcessor.getInstance().getPersistentWorkers());
                persistenceWorkers.addAll(MetricsStreamProcessor.getInstance().getPersistentWorkers());

                CountDownLatch countDownLatch = new CountDownLatch(persistenceWorkers.size());

                for (final PersistenceWorker<? extends StorageData> worker : persistenceWorkers) {
                    pool.submit(() -> {
                        if (log.isDebugEnabled()) {
                            log.debug("extract {} worker data and save", worker.getClass().getName());
                        }
                        try {
                            ArrayList<PrepareRequest> requests = new ArrayList<>();
                            worker.buildBatchRequests(requests);
                            worker.endOfRound(System.currentTimeMillis() - lastTime);

                            prepareRequests.addAll(requests);
                        } finally {
                            countDownLatch.countDown();
                        }

                    });
                }
                countDownLatch.await();

                if (debug) {
                    log.info("build batch persistence duration: {} ms", System.currentTimeMillis() - startTime);
                }
            } finally {
                timer.finish();
            }

            HistogramMetrics.Timer executeLatencyTimer = executeLatency.createTimer();
            try {
                if (CollectionUtils.isNotEmpty(prepareRequests)) {
                    batchDAO.synchronous(prepareRequests);
                }
            } finally {
                executeLatencyTimer.finish();
            }
        } catch (Throwable e) {
            errorCounter.inc();
            log.error(e.getMessage(), e);
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("Persistence data save finish");
            }

            prepareRequests.clear();
            lastTime = System.currentTimeMillis();
        }

        if (debug) {
            log.info("Batch persistence duration: {} ms", System.currentTimeMillis() - startTime);
        }
    }



}

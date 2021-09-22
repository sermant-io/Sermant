package com.lubanops.apm.core.trace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lubanops.apm.bootstrap.api.APIService;
import com.lubanops.apm.bootstrap.api.HarvestListener;
import com.lubanops.apm.bootstrap.commons.LubanApmConstants;
import com.lubanops.apm.bootstrap.config.AgentConfigManager;
import com.lubanops.apm.bootstrap.exception.ApmRuntimeException;
import com.lubanops.apm.bootstrap.log.LogFactory;
import com.lubanops.apm.bootstrap.plugin.apm.APMCollector;
import com.lubanops.apm.bootstrap.trace.SpanEvent;
import com.lubanops.apm.bootstrap.trace.TraceReportService;
import com.lubanops.apm.bootstrap.utils.AgentUtils;
import com.lubanops.apm.core.api.AgentService;
import com.lubanops.apm.core.common.ConnectionException;
import com.lubanops.apm.core.executor.standalone.ServiceThread;
import com.lubanops.apm.core.transfer.InvokerService;
import com.lubanops.apm.core.utils.ReportDataBuilder;
import com.lubanops.apm.integration.access.MessageIdGenerator;
import com.lubanops.apm.integration.access.inbound.EventDataBody;
import com.lubanops.apm.integration.access.inbound.EventDataRequest;

/**
 * TraceQueueService 发送数据
 * @author
 */
@Singleton
public class TraceReportServiceImpl implements TraceReportService, AgentService {

    private final static int MAX_SPAN_EVENT_COUNT = 512;

    private final static String SPAN_EVENT_DATA_TYPE = "SpanEventData";

    private final static Queue<SpanEvent> SPAN_EVENT_DATA_QUEUE = new ArrayBlockingQueue<SpanEvent>(
            MAX_SPAN_EVENT_COUNT);

    private final static Logger LOGGER = LogFactory.getLogger();

    @Inject
    private InvokerService invokerService;

    private HarvestListener<APMCollector> harvestListener;

    private List<SpanEventSendThread> threadList = new ArrayList<SpanEventSendThread>();

    private boolean closed = false;

    /**
     * exception flag
     */
    private boolean hasException = Boolean.FALSE;

    public TraceReportServiceImpl() {
        int threadCount = AgentConfigManager.getEventThreadCount();
        for (int i = 0; i < threadCount; i++) {
            SpanEventSendThread spanEventSendThread = new SpanEventSendThread();
            spanEventSendThread.start();
            threadList.add(spanEventSendThread);
        }
        harvestListener = new HarvestListener<APMCollector>() {
            @Override
            public void onHarvest(APMCollector collector, long time) {
                collector.monitorQueueSize(SPAN_EVENT_DATA_QUEUE.size(),
                        AgentUtils.getObjectSize(SPAN_EVENT_DATA_QUEUE));
            }
        };
        APMCollector.INSTANCE.listenHarvest(harvestListener);
    }

    @Override
    public void init() throws ApmRuntimeException {
    }

    @Override
    public void dispose() throws ApmRuntimeException {
        if (closed) {
            return;
        }
        closed = true;
        int timeout = LubanApmConstants.DEFAULT_SERVICE_SHUTDOWN_TIMEOUT;
        if (timeout > 0) {
            long start = System.currentTimeMillis();
            while (SPAN_EVENT_DATA_QUEUE.isEmpty() && System.currentTimeMillis() - start < timeout) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
        SPAN_EVENT_DATA_QUEUE.clear();
        for (SpanEventSendThread spanEventSendThread : threadList) {
            spanEventSendThread.shutdown(true);
        }
        threadList.clear();
    }

    @Override
    public int getPriority() {
        return AGENT_INTERNAL_SERVICE;
    }

    @Override
    public void offerEvent(SpanEvent spanEvent) {
        if (spanEvent == null) {
            return;
        }
        boolean success = SPAN_EVENT_DATA_QUEUE.offer(spanEvent);
        if (!success) {
            String json = APIService.getJsonApi().toJSONString(spanEvent);
            int length = json.getBytes().length;
            APMCollector.onDiscard(SPAN_EVENT_DATA_TYPE, length);
            LogFactory.getLogger().warning("spanevent data queue is full,data discarded:" + spanEvent.toString());
        } else {
            APMCollector.onStart(SPAN_EVENT_DATA_TYPE, SPAN_EVENT_DATA_QUEUE.size());
        }
    }

    public class SpanEventSendThread extends ServiceThread {
        private EventDataRequest request;

        private long length;

        @Override
        public void run() {
            LOGGER.info("[TRACE REPORTER]reporter start.");
            while (!this.isStopped() && report()) {

            }
            LOGGER.info("[TRACE REPORTER]reporter stop.");
        }

        public boolean report() {
            SpanEvent spanEvent = null;
            long endTime = 0;
            long startTime = 0;
            String logClassName = null;
            String logMethodName = null;
            try {
                spanEvent = SPAN_EVENT_DATA_QUEUE.poll();
                if (spanEvent == null) {
                    super.waitForRunning(50);
                    return true;
                }
                startTime = System.currentTimeMillis();
                request = new EventDataRequest();
                request.setMessageId(MessageIdGenerator.generateMessageId());
                request.setHeader(ReportDataBuilder.buildEventDataHeader());
                request.getHeader().setNeedResponse(false);
                EventDataBody eventDataBody = ReportDataBuilder.buildEventDataBody(spanEvent);
                request.setBody(eventDataBody);
                length = request.getBodyBytes().length;
                if (length > 1000000) {
                    LOGGER.log(Level.SEVERE, "data过大:" + request.getBodyString().substring(0, 1000));
                    Map<String, String> bodyMap = new HashMap<String, String>();
                    bodyMap.put("msg", "数据超过1M已丢弃");
                    eventDataBody.setTags(bodyMap);
                    length = eventDataBody.toString().getBytes().length;
                }
                logClassName = request.getBody().getClassName();
                logMethodName = request.getBody().getMethod();
                sendSpanEvent();
            } catch (Throwable e) {
                Level level = Level.SEVERE;
                if (hasException) {
                    level = Level.FINE;
                }
                LOGGER.log(level,
                        "failed to send data,error message:" + e.getMessage() + ",type:" + e.getClass().getName(),
                        e);
                LOGGER.log(Level.SEVERE, "data:" + logClassName + "." + logMethodName);
                APMCollector.onThrowable(LubanApmConstants.SPAN_EVENT_DATA_TYPE, length, e);
                hasException = true;
            } finally {
                if (startTime > 0) {
                    endTime = System.currentTimeMillis();
                    APMCollector.onFinally(LubanApmConstants.SPAN_EVENT_DATA_TYPE, endTime - startTime);
                }
            }
            return true;
        }

        public void sendSpanEvent() throws ConnectionException, IOException {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, String.format("[debug mode]sending message:[%s]", request.getBodyString()));
            }
            if (invokerService.isSendEnable()) {
                invokerService.sendDataReport(request);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE,
                        String.format("[debug mode]sending message success:[%s]", request.getMessageId()));
                }
            } else {
                APMCollector.onDiscard(SPAN_EVENT_DATA_TYPE, length);
            }
            APMCollector.onSuccess(LubanApmConstants.SPAN_EVENT_DATA_TYPE, length);

        }

        @Override
        public String getServiceName() {
            return TraceReportServiceImpl.class.getSimpleName();
        }

        @Override
        protected void onWaitEnd() {

        }
    }
}

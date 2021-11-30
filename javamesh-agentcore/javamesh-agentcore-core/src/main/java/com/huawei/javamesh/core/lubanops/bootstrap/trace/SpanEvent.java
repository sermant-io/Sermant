package com.huawei.javamesh.core.lubanops.bootstrap.trace;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import com.huawei.javamesh.core.lubanops.bootstrap.agent.AgentInfo;
import com.huawei.javamesh.core.lubanops.bootstrap.config.ConfigManager;
import com.huawei.javamesh.core.lubanops.bootstrap.config.IdentityConfigManager;
import com.huawei.javamesh.core.lubanops.bootstrap.plugin.common.DefaultStats;
import com.huawei.javamesh.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.javamesh.core.lubanops.bootstrap.utils.ThreadUtils;

/**
 * 调用链spanevent对象
 * @author
 * @author
 */
public class SpanEvent {

    private static Timer timer = new Timer("apm-threadstack-collector", true);

    private static ThreadMXBean tmx = ManagementFactory.getThreadMXBean();

    // ~~ elements of event

    /**
     * vTraceId，虚拟traceId，一个vTraceId对应多个实际的traceId， vTraceId会从开始一直往下应用传输
     */
    private String globalTraceId;

    /**
     * 虚拟traceId经过的path路径
     */
    private String globalPath;

    /**
     * event对应的traceId
     */
    private String traceId;

    /**
     * 代表一个rpc调用，格式是1-1-2 这种格式，如果为1 代表是
     */
    private String spanId;

    /**
     * event的ID，在一个具体的span下面event的编号，一般是1-1-2 这种格式。如果是1 代表的是根部的event
     */
    private String eventId;

    private String sourceEventId;

    /**
     * 可能为空，只有发生rpc调用之后才会有
     */
    private String nextSpanId;

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String method;

    /**
     * 类型，mysql，kafka等
     */
    private String type;

    /**
     * 只有是根event也就是span的时候有值
     */
    private String source;

    private String realSource;

    /**
     * 调用起始时间，单位 毫秒
     */
    private long startTime;

    /**
     * 耗时的纳秒数
     */
    private long timeUsed;

    /**
     * 子event的个数
     */
    private int childrenEventCount;

    /**
     * 丢弃的子event个数，key是类型
     */
    private List<DiscardInfo> discard = new ArrayList<SpanEvent.DiscardInfo>();

    private Map<String, DiscardInfo> discardMap = new HashMap<String, SpanEvent.DiscardInfo>();

    private long discardSpanEventStartTime;

    /**
     * 是否有错误，主要用在span的场景，如果一个span的event调用有log.error或者抛出异常，（根据用户的配置来）都认为是有错误
     */
    private boolean hasError;

    /**
     * 错误类型 主要有这么几种 ErrorType枚举的几种，可以逗号分隔多种类型
     */
    private String errorReasons;

    /**
     * 是否异步的event
     */
    private boolean isAsync = false;

    /**
     * 界面展示的参数，每个类型的event自己来实现
     */
    private String argument;

    /**
     * 前端传的分组的id，格式是 xxx.xxx.xxx的样式，有几个span信息可能采用相同的groupId
     */
    private String groupId;

    private boolean hasDbAction;

    private int code;

    private Map<String, String> tags = new LinkedHashMap<String, String>();

    // ~~ event tree model

    /**
     * 根spanevent
     */
    private SpanEvent rootSpanEvent;

    /**
     * 父spanevent
     */
    private SpanEvent parentSpanEvent;

    // ~~ id generator

    /**
     * 子spaneventid生成
     */
    private AtomicInteger atomicId = new AtomicInteger(0);

    /**
     * nextspanid生成
     */
    private AtomicInteger nextSpanAtomicId = new AtomicInteger(0);

    /**
     * 不采集的spanEvent当前深度
     */
    private AtomicInteger disableDeep = new AtomicInteger(0);

    private long nanoTime;

    private Long threadId;

    private TimerTask task;

    // ~~ public methods
    public SpanEvent(String traceId, String spanId, String domainId) {
        if (StringUtils.isBlank(traceId)
                || "null".equals(traceId)
                || StringUtils.isBlank(spanId)
                || "null".equals(spanId)
                || !(String.valueOf(IdentityConfigManager.getDomainId()).equals(domainId))) {
            traceId = AgentInfo.generateTraceId();
            spanId = "1";
        }
        this.setEventId("1");
        this.setTraceId(traceId);
        this.setSpanId(spanId);
    }

    public SpanEvent(SpanEvent parent) {
        this.parentSpanEvent = parent;
        if (parent.getRoot() == null) {
            setRoot(parent);
        } else {
            setRoot(parent.getRoot());
        }
        setTraceId(parent.getTraceId());
        setSpanId(parent.getSpanId());
        setEventId(parent.getChildSpanEventId());
    }

    public String getChildSpanEventId() {
        StringBuilder spanEventId = new StringBuilder();
        spanEventId.append(this.getEventId()).append("-").append(atomicId.incrementAndGet());
        childrenEventCount = childrenEventCount + 1;
        return spanEventId.toString();
    }

    public String generateNextSpanId() {
        String result;
        if (rootSpanEvent != null) {
            result = rootSpanEvent.generateNextSpanId();
        } else {
            StringBuilder nextSpanId = new StringBuilder();
            nextSpanId.append(this.getSpanId()).append("-").append(nextSpanAtomicId.incrementAndGet());
            result = nextSpanId.toString();
            return result;
        }
        setNextSpanId(result);
        return result;
    }

    // ~~ getter and setter
    public SpanEvent getRoot() {
        return rootSpanEvent;
    }

    public void setRoot(SpanEvent rootSpanEvent) {
        this.rootSpanEvent = rootSpanEvent;
    }

    public void setSpanError(boolean hasError) {
        if (rootSpanEvent != null) {
            rootSpanEvent.setHasError(hasError);
        }
        this.setHasError(hasError);
    }

    public long getStartNanoTime() {
        return nanoTime;
    }

    public void setStartNanoTime(long nanoTime) {
        this.nanoTime = nanoTime;
    }

    public SpanEvent getParent() {
        return parentSpanEvent;
    }

    public void setParent(SpanEvent parentSpanEvent) {
        this.parentSpanEvent = parentSpanEvent;
    }

    public int getChildSpanEventCount() {
        return atomicId.get();
    }

    public int getDisableDeep() {
        return disableDeep.get();
    }

    public void addDisableDeep() {
        disableDeep.incrementAndGet();
    }

    public void subDisableDeep() {
        disableDeep.decrementAndGet();
    }

    public void addTag(String key, String value) {
        if (key == null || value == null) {
            return;
        }
        this.tags.put(key, value);
    }

    public void addTag(String key, String value, int limit) {
        if (key == null || value == null) {
            return;
        }
        value = StringUtils.stringTruncat(value, limit, "...");
        tags.put(key, value);
    }

    public void setGlobalTraceId(String globalTraceId) {
        this.globalTraceId = globalTraceId;
    }

    public String getGlobalTraceId() {
        return globalTraceId;
    }

    public void setGlobalPath(String globalPath) {
        this.globalPath = globalPath;
    }

    public String getGlobalPath() {
        return globalPath;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setNextSpanId(String nextSpanId) {
        this.nextSpanId = nextSpanId;
    }

    public String getNextSpanId() {
        return nextSpanId;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimeUsed() {
        return timeUsed;
    }

    public void setTimeUsed(long timeUsed) {
        // 纳秒转毫秒
        this.timeUsed = timeUsed / DefaultStats.NANO_TO_MILLI;
    }

    public int getChildrenEventCount() {
        return childrenEventCount;
    }

    public void setChildrenEventCount(int childrenEventCount) {
        this.childrenEventCount = childrenEventCount;
    }

    public boolean getHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public String getErrorReasons() {
        return errorReasons;
    }

    public void setErrorReasons(String errorReasons) {
        this.errorReasons = errorReasons;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setRealSource(String realSource) {
        this.realSource = realSource;
    }

    public String getRealSource() {
        return realSource;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isHasDbAction() {
        return hasDbAction;
    }

    public void setHasDbAction(boolean hasDbAction) {
        this.hasDbAction = hasDbAction;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public SpanEvent getRootSpanEvent() {
        return rootSpanEvent;
    }

    public void setRootSpanEvent(SpanEvent rootSpanEvent) {
        this.rootSpanEvent = rootSpanEvent;
    }

    public SpanEvent getParentSpanEvent() {
        return parentSpanEvent;
    }

    public void setParentSpanEvent(SpanEvent parentSpanEvent) {
        this.parentSpanEvent = parentSpanEvent;
    }

    public AtomicInteger getAtomicId() {
        return atomicId;
    }

    public void setAtomicId(AtomicInteger atomicId) {
        this.atomicId = atomicId;
    }

    public AtomicInteger getNextSpanAtomicId() {
        return nextSpanAtomicId;
    }

    public void setNextSpanAtomicId(AtomicInteger nextSpanAtomicId) {
        this.nextSpanAtomicId = nextSpanAtomicId;
    }

    public long getNanoTime() {
        return nanoTime;
    }

    public void setNanoTime(long nanoTime) {
        this.nanoTime = nanoTime;
    }

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    public String getSourceEventId() {
        return sourceEventId;
    }

    public void setSourceEventId(String sourceEventId) {
        this.sourceEventId = sourceEventId;
    }

    public Map<String, DiscardInfo> getDiscardMap() {
        return discardMap;
    }

    public void setDiscardMap(Map<String, DiscardInfo> discardMap) {
        this.discardMap = discardMap;
    }

    public List<DiscardInfo> getDiscard() {
        return discard;
    }

    public void setDiscard(List<DiscardInfo> discard) {
        this.discard = discard;
    }

    public long getDiscardSpanEventStartTime() {
        return discardSpanEventStartTime;
    }

    public void setDiscardSpanEventStartTime(long discardSpanEventStartTime) {
        this.discardSpanEventStartTime = discardSpanEventStartTime;
    }

    public void setDiscardInfo() {
        this.discard = new ArrayList<DiscardInfo>(discardMap.values());
    }

    // 不用写get/set方法 不上报这个字段
    public Long threadId() {
        return threadId;
    }

    public void setThreadId(final Long newThreadId) {
        if (ConfigManager.getStackThreshold() > 0) {
            if ("ASYNC_THREAD".equals(type)) {
                return;
            }
            this.threadId = newThreadId;
            if (threadId == null) {
                if (task != null) {
                    task.cancel();
                    task = null;
                }
            } else {
                task = new TimerTask() {
                    @Override
                    public void run() {
                        if (threadId != null) {
                            // 最多打印16行堆栈
                            int maxFrames = 16;
                            ThreadInfo info = tmx.getThreadInfo(threadId, maxFrames);
                            if (info != null) {
                                tags.put("ThreadStack", ThreadUtils.getThreadInfoString(info, maxFrames));
                            }
                        }
                    }
                };
                timer.schedule(task, new Date(startTime + ConfigManager.getStackThreshold()));
            }
        }
    }

    /**
     * 丢弃的信息
     */
    public static class DiscardInfo {

        private String type;

        private int count;

        private long totalTime;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public long getTotalTime() {
            return totalTime;
        }

        public void setTotalTime(long totalTime) {
            this.totalTime = totalTime;
        }
    }
}

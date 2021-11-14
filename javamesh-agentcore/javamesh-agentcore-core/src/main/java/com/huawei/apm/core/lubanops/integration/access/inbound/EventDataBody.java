package com.huawei.apm.core.lubanops.integration.access.inbound;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.huawei.apm.core.lubanops.integration.access.Body;
import com.huawei.apm.core.lubanops.integration.utils.JSON;

/**
 * @author
 * @since 2020/4/30
 **/
public class EventDataBody extends Body {

    /**
     * vTraceId，虚拟traceId，一个vTraceId对应多个实际的traceId， vTraceId会从开始一直往下应用传输
     */
    private String globalTraceId;

    /*
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

    /**
     * 可能为空，只有发生rpc调用之后才会有
     */
    private String nextSpanId;

    /**
     * 产生下一个span的源的eventId
     */
    private String sourceEventId;

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
    private List<DiscardInfo> discard;

    /**
     * 是否有错误，主要用在span的场景，如果一个span的event调用有log.error或者抛出异常，（根据用户的配置来）都认为是有错误
     */
    private Boolean hasError;

    /**
     * 错误类型 主要有这么几种 ErrorType枚举的几种，可以逗号分隔多种类型
     */
    private String errorReasons;

    /**
     * 只有是根event也就是span的时候有值
     */
    private String source;

    private String realSource;

    /**
     * 是否异步的event
     */
    private boolean isAsync = false;

    private Map<String, String> tags = new HashMap<String, String>();

    /**
     * 状态码，只在根调用以及产生next span id的event上具有
     */
    private Integer code;

    /**
     * 界面展示的参数，每个类型的event自己来实现
     */
    private String argument;

    public String getGlobalTraceId() {
        return globalTraceId;
    }

    public void setGlobalTraceId(String globalTraceId) {
        this.globalTraceId = globalTraceId;
    }

    public String getGlobalPath() {
        return globalPath;
    }

    public void setGlobalPath(String globalPath) {
        this.globalPath = globalPath;
    }

    public int getChildrenEventCount() {
        return childrenEventCount;
    }

    public void setChildrenEventCount(int childrenEventCount) {
        this.childrenEventCount = childrenEventCount;
    }

    public List<DiscardInfo> getDiscard() {
        return discard;
    }

    public void setDiscard(List<DiscardInfo> discard) {
        this.discard = discard;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getNextSpanId() {
        return nextSpanId;
    }

    public void setNextSpanId(String nextSpanId) {
        this.nextSpanId = nextSpanId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setTimeUsed(long timeUsed) {
        this.timeUsed = timeUsed;
    }

    public long getTimeUsed() {
        return timeUsed;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setHasError(Boolean hasError) {
        this.hasError = hasError;
    }

    public Boolean getHasError() {
        return hasError;
    }

    public void setErrorReasons(String errorReasons) {
        this.errorReasons = errorReasons;
    }

    public String getErrorReasons() {
        return errorReasons;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync(boolean isAsync) {
        this.isAsync = isAsync;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRealSource() {
        return realSource;
    }

    public void setRealSource(String realSource) {
        this.realSource = realSource;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public void addTag(String key, String value) {
        this.tags.put(key, value);
    }

    public void addTags(Map<String, String> tags) {
        this.tags.putAll(tags);
    }

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getSourceEventId() {
        return sourceEventId;
    }

    public void setSourceEventId(String sourceEventId) {
        this.sourceEventId = sourceEventId;
    }

    /**
     * 丢弃的信息
     */
    public static class DiscardInfo {

        private String type;

        private Integer count;

        private Long totalTime;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public Long getTotalTime() {
            return totalTime;
        }

        public void setTotalTime(Long totalTime) {
            this.totalTime = totalTime;
        }
    }
}

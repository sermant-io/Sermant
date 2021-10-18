package com.huawei.apm.bootstrap.lubanops.config;

/**
 * 系统配置的一些key值 <br>
 * @author
 * @since 2020年3月9日
 */
public class SysConfigKey {

    /**
     * 是否采用加密传输，返回值1是，0就是否
     */
    private static String useSSL = "useSSL";
    /**
     * 调用链采集是否打开
     */
    private static String traceOn = "traceON";

    /**
     * 监控数据是否打开
     */
    private static String monitorOn = "monitorOn";

    /**
     * 子event的最多的个数，如果超过了就省略。默认值100
     */
    private static String childTraceMax = "childTraceMax";

    /**
     * 慢请求的阈值，比如800毫秒，或者1秒，由用户自己定义,这个定义是通用的，对于单个url也可以自己定义自己的慢请求的阈值
     */
    private static String slowRequestThreshold = "globalSlowRequestThrethold";

    /**
     * 慢请求的采样个数，一共四个值格式为 100,50,20,1 分别代表 正常cpu，中位cpu使用，以及高位cpu使用场景，以及单个url的最小采样
     */
    private static String slowRequestTraceCount = "slowRequestTraceCount";

    /**
     * 错误请求的采样个数，一共四个值格式为 100,50,20,1 分别代表
     * 正常cpu，中位cpu使用，以及高位cpu使用场景，以及单个url的最小采样
     */
    private static String errorRequestTranceCount = "errorRequestTraceCount";

    /**
     * 正常的请求采样个数，一共四个值格式为 100,50,20,1 分别代表
     * 正常cpu，中位cpu使用，以及高位cpu使用场景，以及单个url的最小采样
     */
    private static String requestTranceCount = "requestTraceCount";

    /**
     * 是否将异常消息隐藏
     */
    private static String hideExceptionMessage = "hideExceptionMessage";

    /**
     * 采集异常堆栈的长度
     */
    private static String maxExceptionLength = "maxExceptionLength";

    /**
     * 监控数据的最大行数
     */
    private static String maxRows = "maxRows";

    // ~~redis collector system config keys

    /**
     * key of redis body parser config
     */
    private static String collectorParseRedisBody = "parseRedisBody";

    /**
     * key of redis parser length
     */
    private static String collectorParseRedisLength = "parseRedisLength";

    /**
     * transfer config
     */
    private static String useSecureChannel = "useSecureChannel";

    private static String stopAgent = "stopAgent";

    public static String getUseSSL() {
        return useSSL;
    }

    public static void setUseSSL(String useSSL) {
        SysConfigKey.useSSL = useSSL;
    }

    public static String getSlowRequestThreshold() {
        return slowRequestThreshold;
    }

    public static void setSlowRequestThreshold(String slowRequestThreshold) {
        SysConfigKey.slowRequestThreshold = slowRequestThreshold;
    }

    public static String getSlowRequestTraceCount() {
        return slowRequestTraceCount;
    }

    public static void setSlowRequestTraceCount(String slowRequestTraceCount) {
        SysConfigKey.slowRequestTraceCount = slowRequestTraceCount;
    }

    public static String getErrorRequestTranceCount() {
        return errorRequestTranceCount;
    }

    public static void setErrorRequestTranceCount(String errorRequestTranceCount) {
        SysConfigKey.errorRequestTranceCount = errorRequestTranceCount;
    }

    public static String getRequestTranceCount() {
        return requestTranceCount;
    }

    public static void setRequestTranceCount(String requestTranceCount) {
        SysConfigKey.requestTranceCount = requestTranceCount;
    }

    public static String getHideExceptionMessage() {
        return hideExceptionMessage;
    }

    public static void setHideExceptionMessage(String hideExceptionMessage) {
        SysConfigKey.hideExceptionMessage = hideExceptionMessage;
    }

    public static String getMaxExceptionLength() {
        return maxExceptionLength;
    }

    public static void setMaxExceptionLength(String maxExceptionLength) {
        SysConfigKey.maxExceptionLength = maxExceptionLength;
    }

    public static String getMaxRows() {
        return maxRows;
    }

    public static void setMaxRows(String maxRows) {
        SysConfigKey.maxRows = maxRows;
    }

    public static String getCollectorParseRedisBody() {
        return collectorParseRedisBody;
    }

    public static void setCollectorParseRedisBody(String collectorParseRedisBody) {
        SysConfigKey.collectorParseRedisBody = collectorParseRedisBody;
    }

    public static String getCollectorParseRedisLength() {
        return collectorParseRedisLength;
    }

    public static void setCollectorParseRedisLength(String collectorParseRedisLength) {
        SysConfigKey.collectorParseRedisLength = collectorParseRedisLength;
    }

    public static String getUseSecureChannel() {
        return useSecureChannel;
    }

    public static void setUseSecureChannel(String useSecureChannel) {
        SysConfigKey.useSecureChannel = useSecureChannel;
    }

    public static String getStopAgent() {
        return stopAgent;
    }

    public static void setStopAgent(String stopAgent) {
        SysConfigKey.stopAgent = stopAgent;
    }

}

package com.huawei.apm.core.lubanops.integration.access;

/**
 * @author
 * @since 2020/4/30
 **/
public class MessageType {

    /**
     * 监控数据上报
     */
    public static final short MONITOR_DATA_REQUEST = 1;

    public static final short MONITOR_DATA_RESPONSE = 2;

    /**
     * event 数据上报
     */
    public static final short TRACE_EVENT_REQUEST = 3;

    public static final short TRACE_EVENT_RESPONSE = 4;

    /**
     * javaagent链接access server的时候返回的消息，一般是链接错误或者正确的消息
     */
    public static final short ACCESS_SESSION_OPEN_RESPONSE = 5;

    /**
     * 查看javaagent的采集器的状态的消息
     */
    public static final short ACCESS_COLLECTOR_STATUS_REQUEST = 15;

    public static final short ACCESS_COLLECTOR_STATUS_RESPONSE = 16;

    /**
     * 查看是否是response的消息，需要通过合格来判断是否需要实现同步通知的服务
     *
     * @return
     */
    public static boolean isResponseMessage(short type) {
        if (type == MONITOR_DATA_RESPONSE || type == TRACE_EVENT_RESPONSE || type == ACCESS_COLLECTOR_STATUS_RESPONSE) {
            return true;
        }
        return false;
    }
}

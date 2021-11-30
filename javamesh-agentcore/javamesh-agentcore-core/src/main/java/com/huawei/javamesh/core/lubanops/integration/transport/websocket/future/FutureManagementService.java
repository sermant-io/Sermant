package com.huawei.javamesh.core.lubanops.integration.transport.websocket.future;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.javamesh.core.lubanops.integration.Constants;
import com.huawei.javamesh.core.lubanops.integration.access.Message;
import com.huawei.javamesh.core.lubanops.integration.utils.APMThreadFactory;

/**
 * @author
 * @since 2020/5/7
 **/
public class FutureManagementService {

    private final static Logger LOGGER = LogFactory.getLogger();

    private static FutureManagementService instance = new FutureManagementService();

    private Map<Long, MessageFuture> futureMap = new ConcurrentHashMap<Long, MessageFuture>();

    /**
     * 实现数据清理的future，防止用户获取MessageFuture 对象后不调用get方法导致内存泄漏
     */
    private ScheduledExecutorService checkFutureExecutor;

    private FutureManagementService() {

        checkFutureExecutor = new ScheduledThreadPoolExecutor(1, new APMThreadFactory("FutureManagementService", true));
        /**
         * 启动定时清理线程，将过期的future释放，防止内存泄漏
         */
        checkFutureExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    Set<Map.Entry<Long, MessageFuture>> futureSet = futureMap.entrySet();
                    Iterator<Map.Entry<Long, MessageFuture>> it = futureSet.iterator();
                    while (it.hasNext()) {
                        Map.Entry<Long, MessageFuture> entry = it.next();
                        MessageFuture future = entry.getValue();
                        if (future.getFutureAge() > Constants.SYNC_SEND_MESSAGE_TIMEOUT) { // 如果超时了，就删除
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.log(Level.FINE,
                                        String.format("----------timeout,messageid[%s]",
                                                entry.getValue().getMessage()));
                            }
                            it.remove();
                        }
                    }
                } catch (Exception e) {

                }
            }
        }, Constants.SYNC_SEND_MESSAGE_TIMEOUT, Constants.SYNC_SEND_MESSAGE_TIMEOUT * 5, TimeUnit.MILLISECONDS);
    }

    public static FutureManagementService getInstance() {
        return instance;
    }

    public MessageFuture getFuture(long messageId) {
        MessageFuture future = new MessageFuture(this, messageId, Constants.SYNC_SEND_MESSAGE_TIMEOUT);
        futureMap.put(messageId, future);
        return future;
    }

    /**
     * 异步消息达到，通知获取结果
     * @param message
     */
    public void notifyFuture(Message message) {
        MessageFuture future = futureMap.remove(message.getMessageId());
        if (future != null) {
            future.taskFinished(message);
        }
    }

    public MessageFuture removeFuture(long id) {
        return futureMap.remove(id);
    }

}

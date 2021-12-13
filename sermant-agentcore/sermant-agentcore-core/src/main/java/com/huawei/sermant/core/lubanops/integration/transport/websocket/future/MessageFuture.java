/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.sermant.core.lubanops.integration.transport.websocket.future;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.sermant.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.sermant.core.lubanops.integration.access.Message;

/**
 * @author
 * @since 2020/5/7
 **/
public class MessageFuture {

    private final static Logger LOGGER = LogFactory.getLogger();

    public FutureManagementService futureManagement;

    private CountDownLatch latch = new CountDownLatch(1);

    private Message message;

    private long messageId;

    /**
     * future产生的时间，用于清除长时间的future对象
     */
    private long timestamp = System.currentTimeMillis();

    private long elapse1 = 0;

    private long elapse2 = 0;

    private long timeout;

    public MessageFuture(FutureManagementService futureManagement, long messageId, long timeout) {
        this.futureManagement = futureManagement;
        this.messageId = messageId;
        this.timeout = timeout;
    }

    /**
     * 获取计算结果,不管是否超时，都需要将缓存中的清除掉
     * @return
     * @throws InterruptedException
     */
    public Message get() {
        try {
            latch.await(timeout, TimeUnit.MILLISECONDS);
            long now = System.currentTimeMillis();
            elapse1 = now - timestamp;
            if (elapse1 > 200 && LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE,
                        String.format("message finished,id[%d]born[%d]elapse1[%d]", timestamp, messageId, elapse1));
            }
            futureManagement.removeFuture(this.messageId);
            return message;
        } catch (InterruptedException e) {
            return null;
        }

    }

    /**
     * future 生成的时间
     * @return
     */
    public long getFutureAge() {
        return System.currentTimeMillis() - timestamp;
    }

    /**
     * 告诉任务已经完成
     */
    public void taskFinished(Message message) {
        this.message = message;
        long now = System.currentTimeMillis();
        elapse1 = now - timestamp;
        if (elapse1 > 200 && LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE,
                    String.format("message finished,id[%d]born[%d]elapse1[%d]", timestamp, messageId, elapse1));
        }
        latch.countDown();
        elapse2 = System.currentTimeMillis() - now;
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE,
                String.format("message finished,id[%d]born[%d]elapse1[%d]elapse2[%d]", timestamp, messageId, elapse1,
                        elapse2));
        }
    }

    public Message getMessage() {
        return message;
    }

}

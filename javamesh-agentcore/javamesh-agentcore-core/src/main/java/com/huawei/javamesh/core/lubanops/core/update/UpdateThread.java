/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.lubanops.core.update;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.javamesh.core.lubanops.bootstrap.collector.UpdataListenerManager;
import com.huawei.javamesh.core.lubanops.bootstrap.collector.UpdateListener;
import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;

public class UpdateThread extends Thread {
    private final static Logger LOG = LogFactory.getLogger();

    private static UpdateThread instance = new UpdateThread();

    private int sleepInterval = 5;

    private UpdateThread() {
        super("LubanopsUpdateThread");
        if (sleepInterval <= 1) {
            LOG.warning("invalid update thread Interval:" + sleepInterval + ",using default value:10");
            sleepInterval = 10;
        }
        setDaemon(true);
    }

    public static UpdateThread getInstance() {
        return instance;
    }

    @Override
    public void run() {
        LOG.info("UpdateThread started!");
        try {

            while (true) {
                Thread.sleep(sleepInterval * 1000);
                for (UpdateListener ul : UpdataListenerManager.LISTENER_LIST) {
                    try {
                        ul.update();
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "failed to execute update", e);
                    }
                }
            }

        } catch (InterruptedException e) {
            LOG.info("update thread exit!");
        }
    }

    /**
     * 其他线程
     */
    public void shutdown() {
        this.interrupt();
        try {
            this.join();
        } catch (InterruptedException e) {
            LOG.log(Level.WARNING, "failed to join", e);
        }
    }

}

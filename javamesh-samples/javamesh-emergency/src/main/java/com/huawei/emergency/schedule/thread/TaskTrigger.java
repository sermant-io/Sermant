/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.schedule.thread;

import com.huawei.common.api.CommonResult;
import com.huawei.emergency.service.EmergencyPlanService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 任务被调度后，由此触发任务执行
 *
 * @author y30010171
 * @since 2021-11-19
 **/
@Component
public class TaskTrigger {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskTrigger.class);

    private volatile boolean isTriggerStop = false;
    private Thread triggerThread;
    private volatile LinkedBlockingQueue<Integer> plans = new LinkedBlockingQueue<>(1024);

    @Autowired
    private EmergencyPlanService planService;

    public void trigger(int planId) {
        try {
            plans.offer(planId, 1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("Timeout to trigger plan {}. {}", planId, e.getMessage());
        }
    }

    @PostConstruct
    public void start() {
        triggerThread = new Thread(() -> {
            LOGGER.info("The task-trigger start.");
            List<Integer> allPlans = new ArrayList<>();
            while (!isTriggerStop) {
                try {
                    plans.drainTo(allPlans);
                    for (int i = 0; i < allPlans.size(); i++) {
                        if (allPlans.get(i) != null) {
                            long start = System.currentTimeMillis();
                            CommonResult result = planService.exec(allPlans.get(i), "system");
                            LOGGER.debug("trigger cost {} ms. {}", System.currentTimeMillis() - start, result.getMsg());
                        }
                    }
                } catch (Exception e) {
                    if (!isTriggerStop) {
                        LOGGER.error(e.getMessage(), e);
                    }
                } finally {
                    allPlans.clear();
                }
            }
            LOGGER.info("The task-trigger stop.");
        }, "task-trigger");
        triggerThread.setDaemon(true);
        triggerThread.start();
    }

    @PreDestroy
    public void stop() {
        if (plans.size() > 0) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        isTriggerStop = true;
        if (Thread.State.TERMINATED != triggerThread.getState()) {
            try {
                triggerThread.join();
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}

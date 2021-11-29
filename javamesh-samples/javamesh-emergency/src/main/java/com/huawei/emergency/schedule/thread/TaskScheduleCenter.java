/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.schedule.thread;

import com.huawei.common.constant.ScheduleType;
import com.huawei.common.constant.ValidEnum;
import com.huawei.emergency.entity.EmergencyPlan;
import com.huawei.emergency.entity.EmergencyPlanExample;
import com.huawei.emergency.mapper.EmergencyPlanMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 任务调度器，以预案为单位
 *
 * @author y30010171
 * @since 2021-11-19
 **/
@Component
public class TaskScheduleCenter {
    /**
     * 预读时间
     */
    public static final long PRE_READ = 5000L;
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskScheduleCenter.class);
    private static final String SCHEDULE_THREAD_NAME = "task-schedule";
    private static final String CONSUMER_THREAD_NAME = "task-consumer";

    private static volatile Map<Integer, List<Integer>> scheduledPlans = new ConcurrentHashMap<>();

    private volatile boolean isScheduleStop = false;
    private Thread scheduleThread;
    private Thread consumeScheduleThread;
    private volatile boolean isConsumeStop = false;

    @Autowired
    private EmergencyPlanMapper planMapper;

    @Autowired
    private TaskTrigger trigger;

    public TaskScheduleCenter() {
        scheduleThread = new Thread(() -> {
            LOGGER.info("The {} is running now.", SCHEDULE_THREAD_NAME);
            try {
                TimeUnit.MILLISECONDS.sleep(PRE_READ - System.currentTimeMillis() % 1000);
            } catch (InterruptedException e) {
                if (!isScheduleStop) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            while (!isScheduleStop) {
                long now = System.currentTimeMillis();
                List<EmergencyPlan> scheduledPlans = new ArrayList<>();
                try {
                    EmergencyPlanExample needScheduled = new EmergencyPlanExample();
                    needScheduled.createCriteria()
                        .andIsValidEqualTo(ValidEnum.VALID.getValue())
                        .andScheduleStatusEqualTo(ValidEnum.VALID.getValue())
                        .andTriggerNextTimeLessThan(now + PRE_READ);
                    scheduledPlans = planMapper.selectByExample(needScheduled);
                    scheduledPlans.forEach(plan -> {
                        if (now > plan.getTriggerNextTime() + PRE_READ) {
                            LOGGER.warn("plan {} is misfire.", plan.getPlanId());

                            // todo misFire
                            refreshNextTriggerTime(plan, new Date());
                        } else if (now > plan.getTriggerNextTime()) { // 触发时间在的前五秒内
                            // 触发一次
                            trigger.trigger(plan.getPlanId());
                            refreshNextTriggerTime(plan, new Date(plan.getTriggerNextTime()));

                            // 下次触发时间，还处于预读区间
                            if ("1".equals(plan.getScheduleStatus()) && now + PRE_READ > plan.getTriggerNextTime()) {
                                int second = (int) ((plan.getTriggerNextTime() / 1000) % 60);
                                pushScheduledPlan(second, plan.getPlanId());
                                refreshNextTriggerTime(plan, new Date(plan.getTriggerNextTime()));
                            }
                            LOGGER.info("plan {} was scheduled.", plan.getPlanId());
                        } else {
                            int second = (int) ((plan.getTriggerNextTime() / 1000) % 60);
                            pushScheduledPlan(second, plan.getPlanId());
                            refreshNextTriggerTime(plan, new Date(plan.getTriggerNextTime()));
                            LOGGER.info("plan {} was scheduled.", plan.getPlanId());
                        }
                    });
                } catch (Exception e) {
                    LOGGER.error("Scan scheduled plans error.", e);
                }
                if (System.currentTimeMillis() - now < 1000) {  // 如果超过一秒 则立马进行下次扫描
                    try {
                        // 如果当前周期没有待执行任务，则跳过该周期。有则扫描每一秒
                        TimeUnit.MILLISECONDS.sleep((scheduledPlans.size() > 0 ? 1000 : PRE_READ) - System.currentTimeMillis() % 1000);
                    } catch (InterruptedException e) {
                        LOGGER.error("Skip period error.", e);
                    }
                }
            }
            LOGGER.info("The {} stop.", SCHEDULE_THREAD_NAME);
        }, SCHEDULE_THREAD_NAME);
        scheduleThread.setDaemon(true);
        consumeScheduleThread = new Thread(() -> {
            LOGGER.info("The {} is running now.", CONSUMER_THREAD_NAME);
            while (!isConsumeStop) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000 - System.currentTimeMillis() % 1000);
                } catch (InterruptedException e) {
                    if (!isConsumeStop) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                try {
                    List<Integer> plans = new ArrayList<>();
                    int nowSecond = Calendar.getInstance().get(Calendar.SECOND);
                    for (int i = 0; i < 2; i++) {
                        List<Integer> tmpData = scheduledPlans.remove((nowSecond + 60 - i) % 60); // 避免处理耗时太，每次获取数据向前获取1秒
                        if (tmpData != null) {
                            plans.addAll(tmpData);
                        }
                    }
                    if (plans.size() > 0) {
                        LOGGER.debug("trigger scheduled plan at second {} -> {}", nowSecond, Arrays.asList(plans));
                        plans.forEach(trigger::trigger);
                        plans.clear();
                    }
                } catch (Exception e) {
                    LOGGER.error("Consume scheduled plans error.", e);
                }
            }
            LOGGER.info("The {} stop.", CONSUMER_THREAD_NAME);
        }, CONSUMER_THREAD_NAME);
        consumeScheduleThread.setDaemon(true);
    }

    /**
     * 开始调度
     */
    @PostConstruct
    public void start() {
        scheduleThread.start();
        consumeScheduleThread.start();
    }

    @PreDestroy
    public void stop() {
        isScheduleStop = true;
        if (scheduleThread.getState() != Thread.State.TERMINATED) {
            try {
                scheduleThread.join();
            } catch (InterruptedException e) {
                LOGGER.error("Waiting scheduleThread finished error.", e);
            }
        }

        boolean hasScheduledPlans = false;
        if (!scheduledPlans.isEmpty()) {
            for (int second : scheduledPlans.keySet()) {
                List<Integer> plans = scheduledPlans.get(second);
                if (plans != null && plans.size() > 0) {
                    hasScheduledPlans = true;
                    break;
                }
            }
        }
        if (hasScheduledPlans) {
            try {
                TimeUnit.SECONDS.sleep(PRE_READ + 2);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        isConsumeStop = true;
        if (consumeScheduleThread.getState() != Thread.State.TERMINATED) {
            try {
                consumeScheduleThread.join();
            } catch (InterruptedException e) {
                LOGGER.error("Waiting consumeScheduleThread finished error.", e);
            }
        }
    }

    private void pushScheduledPlan(int second, int planId) {
        List<Integer> plans = scheduledPlans.get(second);
        if (plans == null) {
            plans = new ArrayList<>();
            scheduledPlans.put(second, plans);
        }
        plans.add(planId);
        LOGGER.debug("push scheduled plan at second {} -> {}", second, plans);
    }


    private void refreshNextTriggerTime(EmergencyPlan plan, Date from) {
        Date nextTriggerDate = generateNextTriggerTime(plan, from);
        EmergencyPlan updatePlan = new EmergencyPlan();
        updatePlan.setPlanId(plan.getPlanId());
        if (nextTriggerDate != null) {
            updatePlan.setTriggerLastTime(updatePlan.getTriggerNextTime());
            updatePlan.setTriggerNextTime(nextTriggerDate.getTime());
        } else {
            updatePlan.setScheduleStatus(ValidEnum.IN_VALID.getValue());
            updatePlan.setTriggerLastTime(0L);
            updatePlan.setTriggerNextTime(0L);
        }
        plan.setScheduleStatus(updatePlan.getScheduleStatus());
        plan.setTriggerLastTime(updatePlan.getTriggerLastTime());
        plan.setTriggerNextTime(updatePlan.getTriggerNextTime());
        planMapper.updateByPrimaryKeySelective(updatePlan);
    }

    public Date generateNextTriggerTime(EmergencyPlan plan, Date from) {
        ScheduleType scheduleType = ScheduleType.match(plan.getScheduleType(), ScheduleType.NONE);
        if (ScheduleType.CORN == scheduleType) {
            CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(plan.getScheduleConf());
            return cronSequenceGenerator.next(from);
        } else if (ScheduleType.FIX_DATE == scheduleType) {
            return new Date(from.getTime() + Integer.valueOf(plan.getScheduleConf()) * 1000);
        } else if (ScheduleType.ONCE == scheduleType) {
            return from;
        }
        return null;
    }
}

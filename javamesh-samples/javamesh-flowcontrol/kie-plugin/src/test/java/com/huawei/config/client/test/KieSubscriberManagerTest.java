/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.config.client.test;

import com.huawei.config.kie.KieRequest;
import com.huawei.config.kie.KieRequestFactory;
import com.huawei.config.listener.ConfigurationListener;
import com.huawei.config.listener.KvDataHolder;
import com.huawei.config.listener.SubscriberManager;
import org.junit.Test;

import java.util.EventObject;

/**
 * 测试
 *
 * @author zhouss
 * @since 2021-11-18
 */
public class KieSubscriberManagerTest {

    private static final String WAIT = "50";

    @Test
    public void testSubscribe() throws InterruptedException {
        final SubscriberManager instance = SubscriberManager.getInstance();
        instance.subscribe(
                KieRequestFactory.buildKieRequest(new String[]{"label=version:1.0"}),
                new ConfigurationListener() {
                    @Override
                    public void onEvent(EventObject object) {
                        final Object source = object.getSource();
                        if (source instanceof KvDataHolder.EventDataHolder) {
                            KvDataHolder.EventDataHolder eventDataHolder = (KvDataHolder.EventDataHolder) source;
                            System.out.println("added" + eventDataHolder.getAdded());
                            System.out.println("deleted" + eventDataHolder.getDeleted());
                            System.out.println("modified" + eventDataHolder.getModified());
                            System.out.println("========================version 1.0===================================");
                        }
                    }
                });
        instance.subscribe(
                KieRequestFactory.buildKieRequest(new String[]{"label=version:2.0"}),
                new ConfigurationListener() {
                    @Override
                    public void onEvent(EventObject object) {
                        final Object source = object.getSource();
                        if (source instanceof KvDataHolder.EventDataHolder) {
                            KvDataHolder.EventDataHolder eventDataHolder = (KvDataHolder.EventDataHolder) source;
                            System.out.println("added" + eventDataHolder.getAdded());
                            System.out.println("deleted" + eventDataHolder.getDeleted());
                            System.out.println("modified" + eventDataHolder.getModified());
                            System.out.println("=============================version 2.0==============================");
                        }
                    }
                });
        Thread.sleep(10000000);
    }

    @Test
    public void testaaa() throws InterruptedException {
        final SubscriberManager instance = SubscriberManager.getInstance();
        instance.subscribe(buildServiceRequest(), new ConfigurationListener() {
            @Override
            public void onEvent(EventObject object) {
                final Object source = object.getSource();
                if (source instanceof KvDataHolder.EventDataHolder) {
                    KvDataHolder.EventDataHolder eventDataHolder = (KvDataHolder.EventDataHolder) source;
                    System.out.println("added" + eventDataHolder.getAdded());
                    System.out.println("deleted" + eventDataHolder.getDeleted());
                    System.out.println("modified" + eventDataHolder.getModified());
                    System.out.println("=============================service==============================");
                }
            }
        });
        instance.subscribe(buildAppRequest(), new ConfigurationListener() {
            @Override
            public void onEvent(EventObject object) {
                final Object source = object.getSource();
                if (source instanceof KvDataHolder.EventDataHolder) {
                    KvDataHolder.EventDataHolder eventDataHolder = (KvDataHolder.EventDataHolder) source;
                    System.out.println("added" + eventDataHolder.getAdded());
                    System.out.println("deleted" + eventDataHolder.getDeleted());
                    System.out.println("modified" + eventDataHolder.getModified());
                    System.out.println("=============================app==============================");
                }
            }
        });

        instance.subscribe(buildCustomRequest(), new ConfigurationListener() {
            @Override
            public void onEvent(EventObject object) {
                final Object source = object.getSource();
                if (source instanceof KvDataHolder.EventDataHolder) {
                    KvDataHolder.EventDataHolder eventDataHolder = (KvDataHolder.EventDataHolder) source;
                    System.out.println("added" + eventDataHolder.getAdded());
                    System.out.println("deleted" + eventDataHolder.getDeleted());
                    System.out.println("modified" + eventDataHolder.getModified());
                    System.out.println("=============================custom==============================");
                }
            }
        });
        Thread.sleep(10000000);
    }
    private KieRequest buildServiceRequest() {
        return KieRequestFactory.buildKieRequest(WAIT, null,
                        KieRequestFactory.buildLabels(
                                KieRequestFactory.buildLabel("app", "sc"),
                                KieRequestFactory.buildLabel("service", "b"),
                                KieRequestFactory.buildLabel("environment", "production")));
    }

    private KieRequest buildAppRequest() {
        return KieRequestFactory.buildKieRequest(WAIT, null,
                        KieRequestFactory.buildLabels(
                                KieRequestFactory.buildLabel("app", "sc"),
                                KieRequestFactory.buildLabel("environment", "production")));
    }

    private KieRequest buildCustomRequest() {
        return KieRequestFactory.buildKieRequest(WAIT, null,
                        KieRequestFactory.buildLabels(
                                KieRequestFactory.buildLabel("public",
                                        "")));
    }


    @Test
    public void testSubscribeLongRequest() throws InterruptedException {
        final KieRequest kieRequest = KieRequestFactory.buildKieRequest("10", new String[]{"label=version:3.0"});
        final SubscriberManager instance = SubscriberManager.getInstance();
        instance.subscribe(kieRequest, new ConfigurationListener() {
            @Override
            public void onEvent(EventObject object) {
                final Object source = object.getSource();
                if (source instanceof KvDataHolder.EventDataHolder) {
                    KvDataHolder.EventDataHolder eventDataHolder = (KvDataHolder.EventDataHolder) source;
                    System.out.println("added" + eventDataHolder.getAdded());
                    System.out.println("deleted" + eventDataHolder.getDeleted());
                    System.out.println("modified" + eventDataHolder.getModified());
                    System.out.println("=============================version 3.0==============================");
                }
            }
        });
        Thread.sleep(100000);
    }

    @Test
    public void testUnSubscribe() throws InterruptedException {
        final KieRequest kieRequest = KieRequestFactory.buildKieRequest("10", new String[]{"label=version:3.0"});
        final SubscriberManager instance = SubscriberManager.getInstance();
        final ConfigurationListener listener = new ConfigurationListener() {
            @Override
            public void onEvent(EventObject object) {
                final Object source = object.getSource();
                if (source instanceof KvDataHolder.EventDataHolder) {
                    KvDataHolder.EventDataHolder eventDataHolder = (KvDataHolder.EventDataHolder) source;
                    System.out.println("added" + eventDataHolder.getAdded());
                    System.out.println("deleted" + eventDataHolder.getDeleted());
                    System.out.println("modified" + eventDataHolder.getModified());
                    System.out.println("=============================version 3.0==============================");
                }
            }
        };
        instance.subscribe(kieRequest, listener);
        Thread.sleep(11000);
        instance.unSubscribe(kieRequest, listener);
        Thread.sleep(1000000);
    }
}

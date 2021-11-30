package com.huawei.javamesh.core.lubanops.bootstrap.collector;

import java.util.concurrent.CopyOnWriteArrayList;

public class UpdataListenerManager {
    public final static CopyOnWriteArrayList<UpdateListener> LISTENER_LIST = new CopyOnWriteArrayList<UpdateListener>();

    public static void registerListener(UpdateListener listener) {
        LISTENER_LIST.add(listener);
    }
}

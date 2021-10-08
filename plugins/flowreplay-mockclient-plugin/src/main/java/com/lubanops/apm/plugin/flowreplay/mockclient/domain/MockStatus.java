package com.lubanops.apm.plugin.flowreplay.mockclient.domain;

import java.util.HashMap;

public class MockStatus {
    public static ThreadLocal relationContext = new ThreadLocal<HashMap<String, String>>();
}

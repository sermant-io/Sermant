/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */
package com.huawei.flowrecord.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * record flag to make sure mysql weither record or not
 *
 */
public class RecordStatus {
    public static boolean isMysqlRecord = false;

    public static Map<String, String> map = new HashMap<String, String>();

    public static ThreadLocal context = new ThreadLocal<Stack<RecordContext>>();

    public static ThreadLocal relationContext = new ThreadLocal<HashMap<String, String>>();
}

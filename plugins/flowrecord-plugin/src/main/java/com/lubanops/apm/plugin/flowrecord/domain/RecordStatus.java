/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */
package com.lubanops.apm.plugin.flowrecord.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * record flag to make sure mysql weither record or not
 *
 * @author lihongjiang
 * @version 0.1
 * @since 2021-04-13
 */
public class RecordStatus {
    public static boolean isMysqlRecord = false;

    public static Map<String, String> map = new HashMap<String, String>();

    public static ThreadLocal context = new ThreadLocal<Stack<RecordContext>>();

    public static ThreadLocal relationContext = new ThreadLocal<HashMap<String, String>>();
}

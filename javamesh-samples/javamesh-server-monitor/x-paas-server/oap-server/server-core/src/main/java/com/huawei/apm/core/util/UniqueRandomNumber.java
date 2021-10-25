/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.util;

import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.Random;

/**
 * 产生唯一的随机数
 *
 * @author hdy
 * @since 2021-09-10
 */
public class UniqueRandomNumber {
    private volatile static UniqueRandomNumber uniqueRandomNumber;

    private UniqueRandomNumber() {
    }

    public static UniqueRandomNumber uniqueRandomNumber() {
        if (uniqueRandomNumber == null) {
            synchronized (UniqueRandomNumber.class) {
                if (uniqueRandomNumber == null) {
                    uniqueRandomNumber = new UniqueRandomNumber();
                }
            }
        }
        return uniqueRandomNumber;
    }

    /**
     * 获取唯一数的集合
     *
     * @param length 告警信息条数
     * @return 不重复唯一数的集合
     */
    public LinkedList<Integer> getUniqueRandomNumber(int length) {
        LinkedList<Integer> integers = new LinkedList<>();
        Random secureRandom = new SecureRandom();
        while (integers.size() < length) {
            int i = secureRandom.nextInt(1000);
            if (!integers.contains(i)) {
                integers.push(i);
            }
        }
        return integers;
    }
}

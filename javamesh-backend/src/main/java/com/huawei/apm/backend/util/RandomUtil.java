package com.huawei.apm.backend.util;

import org.apache.commons.lang.RandomStringUtils;

import java.util.Random;

public class RandomUtil {

    public Integer getRandomInt(Integer range) {
        Random rand = new Random();
        return rand.nextInt(range) + 1;
    }

    public String getRandomStr(Integer len) {
        return RandomStringUtils.randomAlphanumeric(len);
    }

    public Long getRandomLong(Integer min, Integer max) {
        return min + (((long) (new Random().nextDouble() * (max - min))));
    }
}

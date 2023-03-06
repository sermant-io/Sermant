/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.backend.util;

import org.apache.commons.lang.RandomStringUtils;

import java.util.Random;

public class RandomUtil {

    private final Random random = new Random();

    public Integer getRandomInt(Integer range) {
        return random.nextInt(range) + 1;
    }

    public String getRandomStr(Integer len) {
        return RandomStringUtils.randomAlphanumeric(len);
    }

    public Long getRandomLong(Integer min, Integer max) {
        return min + (((long) (random.nextDouble() * (max - min))));
    }
}

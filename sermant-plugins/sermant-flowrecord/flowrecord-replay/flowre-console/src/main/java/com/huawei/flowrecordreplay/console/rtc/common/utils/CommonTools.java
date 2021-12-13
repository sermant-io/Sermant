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

package com.huawei.flowrecordreplay.console.rtc.common.utils;

import com.huawei.flowrecordreplay.console.rtc.common.redis.RedisUtil;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 公共工具类
 *
 * @author hanpeng
 * @since 2021-04-07
 */
public class CommonTools {
    /**
     * 验证ip：port的合法性的正则表达式
     */
    private static final Pattern IP_PORT_PATTERN = Pattern.compile("^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.)"
            + "{3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)"
            + ":([0-9]|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$");

    private CommonTools() {
    }

    /**
     * 静态方法，获取存入redis中的流控信息的资源key值
     *
     * @param key      键
     * @param resource 资源名
     * @param date     日期
     * @return 返回 封装后的key
     */
    public static String getMetricResourceKey(String key, String resource, Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        int year = localDateTime.getYear();
        int monthValue = localDateTime.getMonthValue();
        int dayOfMonth = localDateTime.getDayOfMonth();
        int hour = localDateTime.getHour();
        return key + ":" + resource + "_" + year + "-" + monthValue + "-" + dayOfMonth + "_" + hour;
    }

    /**
     * 验证是否为数字
     *
     * @param str 字符串
     * @return 布尔值 true表示为数字，false表示为非数字
     */
    public static boolean isNumeric(String str) {
        for (int num = str.length() - 1; num >= 0; num--) {
            if (!Character.isDigit(str.charAt(num))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 主要目的是，为了读取指定的各个分区的消费偏移量
     *
     * @param redisUtil  redis操作类
     * @param consumer   消费者对象
     * @param partitions 要获取偏移量的所有分区
     * @param safeOffset 在配置文件中可配置项，为了防止丢失数据，设置一个在现有的offset靠前消费的值
     */
    public static void consumePartitionOffsetAssigned(RedisUtil redisUtil,
        KafkaConsumer<String, String> consumer,
        Collection<TopicPartition> partitions,
        long safeOffset) {
        for (TopicPartition partition : partitions) {
            // 接收到新任务将当前主题下的分区下的offset查出来，如果没有就从0开始，有就从下一个开始
            String offsetStr = redisUtil.get(partition.topic() + ":" + partition.partition());
            long offset;
            if (offsetStr != null && !"".equals(offsetStr)
                    && CommonTools.isNumeric(offsetStr)
                    && (offset = Integer.parseInt(offsetStr) - safeOffset) > 0) {
                consumer.seek(partition, offset);
            } else {
                consumer.seek(partition, 0);
            }
        }
    }

    /**
     * 验证ip:port字符串格式是否满足ip和port的限定规则：ip范围[0,255]，port范围[0,65535]
     *
     * @param value 待验证的输入值
     * @return 返回是否满足正则，true表示满足，false表示不满足。
     */
    public static boolean validateHostAndPort(String value) {
        Matcher matcher = IP_PORT_PATTERN.matcher(value);
        return matcher.matches();
    }
}

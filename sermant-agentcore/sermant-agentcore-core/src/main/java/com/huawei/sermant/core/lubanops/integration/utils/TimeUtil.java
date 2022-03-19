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

package com.huawei.sermant.core.lubanops.integration.utils;

import com.huawei.sermant.core.lubanops.bootstrap.exception.ApmRuntimeException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * 时间相关的util方法 <br>
 *
 * @author
 * @since 2020年3月4日
 */
public class TimeUtil {

    public final static TimeZone TIMEZONE_SHANGHAI = TimeZone.getTimeZone("Asia/Shanghai");

    public final static Locale LOCALE_SHANGHAI = Locale.SIMPLIFIED_CHINESE;

    //
    public final static long MINUTE = 60 * 1000L;

    //
    public final static long HOUR = 3600 * 1000L;

    //
    public final static long DAY = 24 * 3600 * 1000L;

    /*
     *
     */
    public final static String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 上海时区
     */
    private final static TimeUtil SHANGHAI = new TimeUtil(TIMEZONE_SHANGHAI, LOCALE_SHANGHAI);

    private TimeZone timeZone;

    private Locale locale;

    private TimeUtil(TimeZone timeZone, Locale locale) {
        this.timeZone = timeZone;
        this.locale = locale;
    }

    private TimeUtil() {

    }

    public static TimeUtil getDefaultInstance() {
        return SHANGHAI;
    }

    /*
     *  将时间格式化
     */
    public String formatWithDefault(Date d) {
        SimpleDateFormat df = new SimpleDateFormat(DEFAULT_FORMAT, locale);
        return df.format(d);
    }

    /*
     * 将时间格式化
     */
    public String formatWithDefault(long d) {
        SimpleDateFormat df = new SimpleDateFormat(DEFAULT_FORMAT, locale);
        return df.format(d);
    }

    /*
     * 解析时间
     */
    public Date parseWithDefault(String s) {
        try {
            SimpleDateFormat df = new SimpleDateFormat(DEFAULT_FORMAT, this.locale);

            return df.parse(s);
        } catch (ParseException e) {
            throw new ApmRuntimeException("failed to parseWithDefault", e);
        }

    }

    /*
     * 判断时间是否是一分钟的开始
     */
    public boolean isStartOfMinute(long timeInMillis) {
        return timeInMillis % 60000 == 0;

    }

    /*
     * 将时间trim到一分钟的开始
     */
    public long trimToStartOfMinute(long timeInMillis) {
        return (timeInMillis / MINUTE) * MINUTE;
    }

    /**
     * trim到一分钟的结束
     *
     * @param timeInMillis 时间的毫秒值
     * @return
     */
    public long trimToEndOfMinute(long timeInMillis) {
        return trimToStartOfMinute(timeInMillis) + MINUTE;
    }

    /*
     * 判断时间是否是一个小时的开始
     */
    public boolean isStartOfHour(long timeInMillis) {
        return timeInMillis % HOUR == 0;

    }

    /*
     * 将时间trim到一个小时的开始
     */
    public long trimToStartOfAnHour(long timeInMillis) {
        return (timeInMillis / HOUR) * HOUR;

    }

    /*
     * 将时间trim到一个小时的结束
     */
    public long trimToEndOfAnHour(long timeInMillis) {
        return trimToStartOfAnHour(timeInMillis) + HOUR;
    }

    /*
     *
     * 判断时间是否是一天的开始,天是跟时区相关，不能简单除来计算
     * @param
     */
    public boolean isStartOfDay(long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(this.timeZone);
        cal.setTimeInMillis(timeInMillis);
        //
        return ((cal.get(Calendar.HOUR_OF_DAY) == 0) && (cal.get(Calendar.MINUTE) == 0) && (cal.get(Calendar.SECOND)
                == 0) && (cal.get(Calendar.MILLISECOND) == 0));
    }

    /*
     *
     * 将时间trim到一天的开始
     * @param
     */
    public long trimToStartOfADay(long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(this.timeZone);
        cal.setTimeInMillis(timeInMillis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();

    }

    /*
     * 时间trim到一天的结束
     */
    public long trimToEndOfADay(long timeInMillis) {
        return trimToStartOfADay(timeInMillis) + DAY;
    }

    /*
     * 解析时间
     */
    public Date parseWithFormat(String s, String format, TimeZone zone) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setTimeZone(zone);
            return sdf.parse(s);
        } catch (ParseException e) {
            throw new ApmRuntimeException("failed to parseWithFormat", e);
        }

    }

    private static long currentTimeMillis;
    static Object obj = new Object();

    static {
        currentTimeMillis = System.currentTimeMillis();
        Thread daemon = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    synchronized (obj) {
                        currentTimeMillis = System.currentTimeMillis();
                        try {
                            TimeUnit.MILLISECONDS.sleep(1);
                        } catch (Throwable e) {

                        }
                    }
                }
            }
        });
        daemon.setDaemon(true);
        daemon.setName("sentinel-time-tick-thread");
        daemon.start();
    }

    public static long currentTimeMillis() {
        return currentTimeMillis;
    }

}

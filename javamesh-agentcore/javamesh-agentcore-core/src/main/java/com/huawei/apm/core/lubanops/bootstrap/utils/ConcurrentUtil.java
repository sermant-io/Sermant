package com.huawei.apm.core.lubanops.bootstrap.utils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 并发的类，主要是在高并发场景下 <br>
 * @author
 * @since 2020年3月9日
 */
public class ConcurrentUtil {

    /*
     * 多个线程竞争设置原子整数的最小值
     */
    public static void setMinValue(AtomicInteger current, int value) {
        for (;;) {
            int min = current.get();
            if (value < min) {
                if (current.compareAndSet(min, value)) {
                    break;
                } else {
                    continue;
                }
            } else {
                break;
            }
        }
    }

    /*
     * 多个线程竞争设置原子整数的最小值
     */
    public static void setMinValue(AtomicLong current, long value) {
        for (;;) {
            long min = current.get();
            if (value < min) {
                if (current.compareAndSet(min, value)) {
                    break;
                } else {
                    continue;
                }
            } else {
                break;
            }
        }
    }

    /*
     * 多个线程竞争设置原子浮点数的最小值
     */
    public static void setMinValue(AtomicDouble current, double value) {
        for (;;) {
            double min = current.get();
            if (value < min) {
                if (current.compareAndSet(min, value)) {
                    break;
                } else {
                    continue;
                }
            } else {
                break;
            }
        }
    }

    /*
     * 多个线程竞争设置原子整数的最大值
     */
    public static boolean setMaxValue(AtomicInteger current, int value) {
        for (;;) {
            int maxvalue = current.get();
            if (value > maxvalue) {
                if (current.compareAndSet(maxvalue, value)) {
                    return true;
                } else {
                    continue;
                }
            } else {
                return false;
            }
        }
    }

    /*
     * 多个线程竞争设置原子整数的最大值
     */
    public static boolean setMaxValue(AtomicLong current, long value) {
        for (;;) {
            long max = current.get();
            if (value > max) {
                if (current.compareAndSet(max, value)) {
                    return true;
                } else {
                    continue;
                }
            } else {
                return false;
            }
        }
    }

    /*
     * 多个线程竞争设置原子浮点数的最大值
     */
    public static void setMaxValue(AtomicDouble current, double value) {
        for (;;) {
            double max = current.get();
            if (value > max) {
                if (current.compareAndSet(max, value)) {
                    break;
                } else {
                    continue;
                }
            } else {
                break;
            }
        }
    }

}

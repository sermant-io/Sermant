package com.huawei.oap.redis.complement;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 链路重试数据对象
 *
 * @author hefan
 * @since 2021-06-21
 */
@AllArgsConstructor
class IncompletedTrace {
    /**
     * traceId
     */
    @Getter
    private final String traceId;

    /**
     * 重试次数
     */
    private long retryTimes;

    @Getter
    private long updateTime;

    static IncompletedTrace parse(String traceId, double score) {
        String[] updateTimeAndRetryTimes = String.format("%.1f", score).split("\\.");
        long updateTime = Long.parseLong(updateTimeAndRetryTimes[0]);
        long retryTimes = Long.parseLong(updateTimeAndRetryTimes[1]);
        return new IncompletedTrace(traceId, retryTimes, updateTime);
    }

    double getNewScore() {
        return System.currentTimeMillis() + retryTimes / 10d;
    }

    long increaseRetryTimes() {
        return retryTimes++;
    }

}

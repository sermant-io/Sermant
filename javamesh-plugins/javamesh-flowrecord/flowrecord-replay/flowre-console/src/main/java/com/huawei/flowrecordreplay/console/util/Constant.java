/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowrecordreplay.console.util;

/**
 * 常量定义
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-02-26
 */
public class Constant {
    /**
     * 错误码
     */
    public static final int ERROR_CODE = -1;

    /**
     * 待处理状态
     */
    public static final String PENDING_STATUS = "PENDING";

    /**
     * 运行状态
     */
    public static final String RUNNING_STATUS = "RUNNING";

    /**
     * 停止状态
     */
    public static final String STOPPED_STATUS = "STOPPED";

    /**
     * 完成状态
     */
    public static final String DONE_STATUS = "DONE";

    /**
     * 未处理状态
     */
    public static final String UNHANDLED_STATUS = "UNHANDLED";

    /**
     * 录制任务索引
     */
    public static final String RECORD_JOB_INDEX = "record_jobs";

    /**
     * 回放任务索引
     */
    public static final String REPLAY_JOB_INDEX = "replay_jobs";

    /**
     * 忽略字段表索引
     */
    public static final String IGNORE_FIELDS_INDEX = "ignore_fields";

    /**
     * 回放任务索引
     */
    public static final String REPLAY_SUB_JOB_INDEX = "replay_sub_jobs";

    /**
     * 回放结果索引前缀
     */
    public static final String REPLAY_RESULT_PREFIX = "replay_result_";

    /**
     * 回放节点指标索引
     */
    public static final String REPLAY_METRIC = "replay_metric";

    /**
     * 冒号
     */
    public static final String COLON = ":";

    /**
     * 逗号
     */
    public static final String COMMA = ",";

    /**
     * 时间格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * jobId关键词
     */
    public static final String JOB_ID_KEYWORD = "jobId";

    /**
     * application关键词
     */
    public static final String APP_KEYWORD = "application";

    /**
     * "startTime关键词
     */
    public static final String START_TIME_KEYWORD = "startTime";

    /**
     * endTime关键词
     */
    public static final String END_TIME_KEYWORD = "end";

    /**
     * timeStamp关键词
     */
    public static final String TIME_STAMP_KEYWORD = "timeStamp";

    /**
     * worker关键词
     */
    public static final String REPLAY_WORKER_KEYWORD = "workerName";

    /**
     * recordIndex关键词
     */
    public static final String RECORD_INDEX_KEYWORD = "subJobIndex";

    /**
     * status关键词
     */
    public static final String STATUS_KEYWORD = "status";

    /**
     * status关键词
     */
    public static final String NAME_KEYWORD = "name";

    /**
     * 左斜杠
     */
    public static final String STAR = "*";

    /**
     * 左斜杠
     */
    public static final String SPLIT = "/";

    /**
     * 录制
     */
    public static final String RECORD_JOB = "Record";

    /**
     * 回放
     */
    public static final String REPLAY_JOB = "Replay";

    /**
     * 录制任务zk路径前缀
     */
    public static final String RECORD_PATH_PREFIX = "/record_jobs";

    /**
     * 回放任务zk路径前缀
     */
    public static final String REPLAY_PATH_PREFIX = "/replay_jobs";

    /**
     * 待执行回放子任务zk路径前缀
     */
    public static final String SUB_REPLAY_JOB_PATH_PREFIX = "/replay_tasks";

    /**
     * 待执行回放子任务有序节点名前缀
     */
    public static final String REPLAY_SUB_JOB_NODE_PREFIX = "/replay_sub_job";

    /**
     * replay_worker的zk路径前缀
     */
    public static final String REPLAY_WORKERS_PATH_PREFIX = "/replay_workers";

    /**
     * replay_worker的zk路径前缀
     */
    public static final String CURRENT_JOB_NODE = "current_job";

    /**
     * SCROLL_SIZE
     */
    public static final int SCROLL_SIZE = 5000;

    /**
     * 扫描任务时间范围(ms)
     */
    public static final int TIME_SCAN_RANGE = 45000;

    /**
     * 最大录制数据展示条数
     */
    public static final int MAX_FINDER = 100;

    /**
     * 指定mock的接口存放在zk的路径前缀
     */
    public static final String  MOCK_METHODS_PREFIX = "/mock_methods";

    /**
     * 录制子调用index前缀
     */
    public static final String SUB_CALL_PREFIX = "subcall_";
}
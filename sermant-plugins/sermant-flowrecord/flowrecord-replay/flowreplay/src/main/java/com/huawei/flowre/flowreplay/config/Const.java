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

package com.huawei.flowre.flowreplay.config;

/**
 * 流量回放常量
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-04-17
 */
public class Const {
    /**
     * zookeeper 路径分隔符
     */
    public static final String SPLIT = "/";

    /**
     * 逗号
     */
    public static final String COMMA = ",";

    /**
     * 冒号
     */
    public static final String COLON = ":";

    /**
     * 下划线
     */
    public static final String UNDERLINE = "_";

    /**
     * 回放worker注册的zookeeper路径
     */
    public static final String REPLAY_WORKERS_PATH = "/replay_workers";

    /**
     * 回放子任务的zookeeper路径
     */
    public static final String REPLAY_JOB_PATH = "/replay_jobs";

    /**
     * 回放任务列表的zookeeper路径
     */
    public static final String REPLAY_TASKS_PATH = "/replay_tasks";

    /**
     * 分布式锁的zookeeper路径
     */
    public static final String REPLAY_LOCK_PATH = "/replay_lock";

    /**
     * 临时有序节点前缀
     */
    public static final String REPLAY_LOCK_PREFIX = "/replay_lock/lock_";

    /**
     * 回放子任务的elasticsearch索引
     */
    public static final String REPLAY_SUB_JOBS = "replay_sub_jobs";

    /**
     * 回放任务的elasticsearch索引
     */
    public static final String REPLAY_JOB = "replay_jobs";

    /**
     * 回放节点名称前缀
     */
    public static final String WORKER_NAME_PREFIX = "replay_worker_";

    /**
     * 存储回放结果数据的topic
     */
    public static final String REPLAY_RESULT_TOPIC = "replay_result_";

    /**
     * 存储处理后回放数据的topic
     */
    public static final String REPLAY_DATA_TOPIC = "replay_data_";

    /**
     * 忽略字段存放的表index
     */
    public static final String IGNORE_FIELDS_INDEX = "ignore_fields";

    /**
     * 存放回放节点指标的表index
     */
    public static final String REPLAY_METRIC_INDEX = "replay_metric";

    /**
     * 回放结果存放的index前缀
     */
    public static final String REPLAY_RESULT_INDEX_PREFIX = "replay_result_";

    /**
     * 通过method 查找回放的 key word
     */
    public static final String METHOD_KEYWORD = "method";

    /**
     * 通过traceId 查找回放的 key word
     */
    public static final String TRACE_KEYWORD = "traceId";

    /**
     * 空字符串
     */
    public static final String BLANK = "";

    /**
     * JSON的值为null
     */
    public static final String NULL_STRING = "null";

    /**
     * JSON中接口名字段
     */
    public static final String FIELD_INTERFACE = "interface";

    /**
     * 无效的版本号
     */
    public static final String INVALID_VERSION = "0.0.0";

    /**
     * 滚动查询每一页的数量
     */
    public static final int SCROLL_SIZE = 5000;

    /**
     * 滚动查询一次的时间限制
     */
    public static final long SCROLL_TIME = 5L;

    /**
     * arguments字段
     */
    public static final String ARGUMENTS_FIELD = "arguments";

    /**
     * 流量修改类型Concrete
     */
    public static final String CONCRETE_TYPE = "Concrete";

    /**
     * 流量修改类型Regex
     */
    public static final String REGEX_TYPE = "Regex";

    /**
     * 流量修改类型Date
     */
    public static final String DATE_TYPE = "Date";

    /**
     * 录制任务ID 放入attachments的属性名
     */
    public static final String RECORD_JOB_ID = "recordJobId";

    /**
     * 录制的TraceId 放入attachments的属性名
     */
    public static final String TRACE_ID = "traceId";

    /**
     * 泛化调用接口version
     */
    public static final String VERSION = "version";

    /**
     * 泛化调用组
     */
    public static final String GROUP = "group";

    /**
     * 线程池里空闲线程存活时间
     */
    public static final long KEEP_ALIVE_TIME = 60L;

    /**
     * http 类型标志
     */
    public static final String HTTP_TYPE = "Http";

    /**
     * dubbo 类型标志
     */
    public static final String DUBBO_TYPE = "Dubbo";

    /**
     * environment port 属性
     */
    public static final String SERVER_PORT = "local.server.port";

    /**
     * Date替换limit size
     */
    public static final int LIMIT_SIZE = 2;

    /**
     * Thread sleep time
     */
    public static final int THREAD_SLEEP = 1000;
}

/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */
package com.lubanops.stresstest.redis.jedis.command;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Redis handler Factory
 *
 * @author yiwei
 * @since 2021/11/4
 */
public class HandlerFactory {
    private static final Map<String, Handler> MAP = new HashMap<>();
    private static final Handler DEFAULT_HANDLER = new NoopHandler();

    static {
        String[] noOpMethods = {"AUTH", "CLUSTER"};
        for (String method : noOpMethods) {
            MAP.put(method, DEFAULT_HANDLER);
        }
        String[] firstParamMethodNames = {"GET", "HGET", "HMGET", "HEXISTS", "SET", "SETNX", "SETEX", "EXPIRE",
                "EXPIREAT", "TTL", "MOVE", "GETSET", "DECR", "DECRBY", "INCRBY", "INCR", "APPEND", "SUBSTR", "HSET",
                "HSETNX", "HMSET", "HINCRBY", "HDEL", "HLEN", "HKEYS", "HVALS", "HGETALL", "RPUSH", "LPUSH", "LLEN",
                "LRANGE", "LTRIM", "LINDEX", "LSET", "LREM", "LPOP", "RPOP", "SADD", "SMEMBERS", "SREM", "SPOP",
                "SCARD", "SISMEMBER", "SRANDMEMBER", "ZADD", "ZRANGE", "ZREM", "ZRANK", "ZREVRANK", "ZREVRANGE",
                "ZCARD", "ZSCORE", "ZCOUNT", "SORT", "ZRANGEBYSCORE", "ZREVRANGEBYSCORE", "ZREMRANGEBYRANK",
                "ZREMRANGEBYSCORE", "ZUNIONSTORE", "ZINTERSTORE", "ZLEXCOUNT", "ZRANGEBYLEX", "ZREVRANGEBYLEX",
                "ZREMRANGEBYLEX", "LPUSHX", "PERSIST", "RPUSHX", "LINSERT", "SETBIT", "GETBIT", "BITPOS", "SETRANGE",
                "GETRANGE", "BITCOUNT", "DUMP", "RESTORE", "PEXPIRE", "PEXPIREAT", "PTTL", "INCRBYFLOAT", "PSETEX",
                "HINCRBYFLOAT", "GEOADD", "GEODIST", "GEOHASH", "GEOPOS", "GEORADIUS", "GEORADIUSBYMEMBER", "BITFIELD"
        };
        Handler firstHandler = new ModifyFirstParamHandler();
        for (String method : firstParamMethodNames) {
            MAP.put(method, firstHandler);
        }
        String[] allParamsMethodNames = {  "DEL", "EXISTS", "TYPE", "RENAME", "RENAMENX", "RPOPLPUSH", "SINTER",
                "SINTERSTORE", "SUNION", "SUNIONSTORE", "SDIFF", "SDIFFSTORE", "BLPOP", "BRPOP", "BITOP", "PFCOUNT",
                "PFMERGE", "MGET"};
        Handler allHandler = new ModifyAllParamsHandler();
        for (String method : allParamsMethodNames) {
            MAP.put(method, allHandler);
        }
        String[] skipValueMethodNames = {"MSET", "MSETNX"};
        Handler skipValuesHandler = new ModifySkipValueHandler();
        for (String method : skipValueMethodNames) {
            MAP.put(method, skipValuesHandler);
        }
        String[] firstTwoMethodNames = {"SMOVE", "BRPOPLPUSH"};
        Handler firstTwoHandler = new ModifyFirstTwoParamsHandler();
        for (String method : firstTwoMethodNames) {
            MAP.put(method, firstTwoHandler);
        }
    }

    /**
     * 获取命令对应的handler
     *
     * @param command 原始的redis command
     * @return 去处理的handler
     */
    public static Handler getHandler(Object command) {
        return MAP.getOrDefault(command.toString().toUpperCase(Locale.ROOT), DEFAULT_HANDLER);
    }
}

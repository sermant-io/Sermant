/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.backend.dao.redis;

import redis.clients.jedis.resps.Tuple;

import java.util.List;
import java.util.Set;

/**
 * Redis interface
 *
 * @author zhp
 * @since 2024-06-25
 */
public interface RedisOperation {
    /**
     * Returns all the keys matching the glob-style pattern as space separated strings
     *
     * @param pattern key
     * @return the keys matching the glob-style pattern as space separated strings
     */
    Set<String> keys(String pattern);

    /**
     * Store key values and specify expiration time
     *
     * @param key Data stored in Redis key
     * @param seconds expiration time
     * @param value Stored values
     * @return Return execution result
     */
    String setex(String key, long seconds, String value);

    /**
     * Set the specified hash field to the specified value.
     *
     * @param key Data stored in Redis key
     * @param field The field names in the hash table
     * @param value Stored values
     * @return If the field already exists, and the HSET just produced an update of the value, 0 is
     * returned, otherwise if a new field is created 1 is returned.
     */
    long hset(String key, String field, String value);

    /**
     * Add the specified member having the specified score to the sorted set stored at key.
     *
     * @param key Data stored in Redis key
     * @param score Sorting required scores
     * @param member Members that need to be added
     * @return 1 if the new element was added, 0 if the element was already a member of the sorted set and the score
     * was updated
     */
    long zadd(String key, double score, String member);

    /**
     * Obtain the all the elements in the sorted set at key with a score between min and max
     * (including elements with score equal to min or max).
     *
     * @param key Data stored in Redis key
     * @param min Minimum score
     * @param max Maximum score
     * @return Return the all the elements in the sorted set at key with a score between min and max
     */
    List<Tuple> zrangeByScoreWithScores(String key, double min, double max);

    /**
     * Obtain the value corresponding to the key in Redis
     *
     * @param key Data stored in Redis key
     * @return the value corresponding to the key in Redis
     */
    String get(String key);

    /**
     * retrieve the value associated to the specified field.
     *
     * @param key Data stored in Redis key
     * @param field The field names in the hash table
     * @return the value associated to the specified field.
     */
    String hget(String key, String field);

    /**
     * Remove the specified field(s) from a hash stored at key
     *
     * @param key Data stored in Redis key
     * @param fields The field names in the hash table
     * @return The number of fields that were removed from the hash, not including specified but non-existing fields.
     */
    long hdel(String key, String[] fields);

    /**
     * Remove the specified member from the sorted set value stored at key.
     *
     * @param key Data stored in Redis key
     * @param members Members that need to be deleted
     * @return 1 if the new element was removed, 0 if the new element was not a member of the set
     */
    long zrem(String key, String[] members);
}

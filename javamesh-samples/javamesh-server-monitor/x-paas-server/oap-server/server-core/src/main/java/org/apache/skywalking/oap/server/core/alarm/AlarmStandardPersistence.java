/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.server.core.alarm;

import com.huawei.apm.core.util.UniqueRandomNumber;

import java.util.LinkedList;
import java.util.List;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.analysis.worker.RecordStreamProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Save the alarm info into storage for UI query.
 */
public class AlarmStandardPersistence implements AlarmCallback {

    private static final Logger logger = LoggerFactory.getLogger(AlarmStandardPersistence.class);

    @Override
    public void doAlarm(List<AlarmMessage> alarmMessage) {
        // updated by huawei 20210910
        LinkedList<Integer> uniqueRandomNumber = UniqueRandomNumber.uniqueRandomNumber()
                .getUniqueRandomNumber(alarmMessage.size());
        alarmMessage.forEach(message -> {
            if (logger.isDebugEnabled()) {
                logger.debug("Alarm message: {}", message.getAlarmMessage());
            }

            AlarmRecord record = new AlarmRecord();
            record.setScope(message.getScopeId());
            record.setId0(message.getId0());
            record.setId1(message.getId1());
            record.setName(message.getName());
            record.setAlarmMessage(message.getAlarmMessage());
            record.setStartTime(message.getStartTime());
            // updated by huawei 20210910.保证TimeBucket唯一
            record.setTimeBucket(TimeBucket.getRecordTimeBucket(message.getStartTime()) + uniqueRandomNumber.pop());
            // huawei update.无损演练：添加复制标签
            record.setCopy(message.getCopy());
            RecordStreamProcessor.getInstance().in(record);
        });
    }
}

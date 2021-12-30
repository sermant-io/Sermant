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

package com.huawei.flowrecord.domain;

public class RecordFlag {
    private static final ThreadLocal<Boolean> IS_ENTRY = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IS_RECORD = new ThreadLocal<>();

    public static void setIsEntry(boolean isEntry) {
        IS_ENTRY.set(isEntry);
    }

    public static boolean getIsEntry(){
        return IS_ENTRY.get();
    }

    public static void setIsRecord(boolean isRecord) {
        IS_RECORD.set(isRecord);
    }

    public static boolean getIsRecord() {
        return IS_RECORD.get();
    }
}

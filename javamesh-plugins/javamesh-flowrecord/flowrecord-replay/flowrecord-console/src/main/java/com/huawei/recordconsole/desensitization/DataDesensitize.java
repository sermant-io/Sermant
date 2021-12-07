/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.recordconsole.desensitization;

import com.huawei.recordconsole.entity.Recorder;

/**
 * 录制数据脱敏接口
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-04-13
 */
public interface DataDesensitize {
    /**
     * dubbo应用录制数据脱敏
     *
     * @param recorder 录制数据
     * @return 脱敏后的录制数据
     */
    Recorder dubboDesensitize(Recorder recorder) throws Exception;
}

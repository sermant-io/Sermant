/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
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

/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.emergency.dto;

import lombok.Data;

/**
 * 日志下载
 *
 * @since 2021-10-30
 */
@Data
public class DownloadLogDto {
    private String log;

    private String scriptName;

    private String sceneName;
}

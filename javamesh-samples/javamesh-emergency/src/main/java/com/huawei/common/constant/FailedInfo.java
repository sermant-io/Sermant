/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.common.constant;

/**
 * 错误信息
 *
 * @since 2021-10-30
 */
public class FailedInfo {
    /**
     * 删除失败
     */
    public static final String DELETE_FAILED = "删除失败";

    /**
     * 删除未全部成功
     */
    public static final String DELETE_NOT_SUCCESS_ALL = "删除未全部成功";

    /**
     * 服务器信息为空
     */
    public static final String SERVER_INFO_NULL = "执行状态为远程执行时，服务器信息不能为空";

    /**
     * 删除脚本失败
     */
    public static final String DELETE_SCRIPT_FROM_SCENE_FAIL = "从场景中删除脚本失败";

    /**
     * 修改场景失败
     */
    public static final String UPDATE_SCENE_FAIL = "修改场景失败";

    private FailedInfo() {
    }
}

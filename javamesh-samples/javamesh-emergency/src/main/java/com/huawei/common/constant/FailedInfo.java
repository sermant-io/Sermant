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

    /**
     * 上传文件失败
     */
    public static final String UPLOAD_FAIL = "文件上传失败";

    /**
     * 脚本信息不存在
     */
    public static final String SCRIPT_NOT_EXISTS = "脚本信息不存在";

    /**
     * 新建脚本失败
     */
    public static final String SCRIPT_CREATE_FAIL = "新建脚本失败";

    /**
     * 参数异常
     */
    public static final String PARAM_INVALID = "参数异常";

    /**
     * 脚本名已存在
     */
    public static final String SCRIPT_NAME_EXISTS = "脚本名已存在";
    public static final String SUBMIT_REVIEW_FAIL = "提审失败";
    public static final String DOWNLOAD_SCRIPT_FAIL = "下载文件失败";
    public static final String INSUFFICIENT_PERMISSIONS = "权限不足";
    public static final String APPROVE_FAIL = "审核失败";

    private FailedInfo() {
    }
}

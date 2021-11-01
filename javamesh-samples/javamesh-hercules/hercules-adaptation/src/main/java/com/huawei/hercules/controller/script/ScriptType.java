/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.script;

import org.springframework.util.StringUtils;
import sun.swing.StringUIClientPropertyKey;

/**
 * 功能描述：脚本类型定义
 *
 * @author z30009938
 * @since 2021-11-01
 */
public enum ScriptType {
    /**
     * groovy脚本类型
     */
    GROOVY_SCRIPT("GROOVY_SCRIPT", "groovy", ".groovy", true),

    /**
     * python脚本类型
     */
    PYTHON_SCRIPT("PYTHON_SCRIPT", "jython", ".py", true),

    /**
     * 文件夹类型
     */
    DIR("DIR", "dir", "", false);

    /**
     * 类型名称
     */
    private final String realName;

    /**
     * 展示类型
     */
    private final String showName;

    /**
     * 文件后缀名
     */
    private final String suffixName;

    /**
     * true：脚本，false：文件夹
     */
    private final boolean isScript;

    ScriptType(String realName, String showName, String suffixName, boolean isScript) {
        this.realName = realName;
        this.showName = showName;
        this.suffixName = suffixName;
        this.isScript = isScript;
    }

    /**
     * 判断类型是不是文件夹
     *
     * @param needValidateFileType 需要检测的文件类型
     * @return 是文件夹返回true，不是文件夹返回false
     */
    public static boolean isFolder(String needValidateFileType) {
        return DIR.realName.equalsIgnoreCase(needValidateFileType);
    }

    /**
     * 判断类型是不是文件
     *
     * @param needValidateFileType 需要检测的文件类型
     * @return 是文件返回true，不是文件返回false
     */
    public static boolean isScript(String needValidateFileType) {
        ScriptType[] scriptTypes = values();
        for (ScriptType scriptType : scriptTypes) {
            if (scriptType.realName.equalsIgnoreCase(needValidateFileType)
                    || scriptType.showName.equalsIgnoreCase(needValidateFileType)) {
                return scriptType.isScript;
            }
        }
        return false;
    }

    /**
     * 根据文件名称和文件类型，获取文件全名
     *
     * @param prefixName 文件去掉后缀名的部分
     * @param fileType   文件类型
     * @return 文件全名
     */
    public static String getWholeScriptName(String prefixName, String fileType) {
        ScriptType[] scriptTypes = values();
        for (ScriptType scriptType : scriptTypes) {
            if (scriptType.showName.equalsIgnoreCase(fileType)
                    || scriptType.realName.equalsIgnoreCase(fileType)) {
                return prefixName + scriptType.suffixName;
            }
        }
        return prefixName;
    }

    /**
     * 根据文件后缀名称获取文件后端类型
     *
     * @param fileName 文件名称
     * @return 文件类型
     */
    public static String getScriptRealType(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return "";
        }
        ScriptType[] scriptTypes = values();
        for (ScriptType scriptType : scriptTypes) {
            if (scriptType.suffixName.equalsIgnoreCase(fileName.substring(fileName.lastIndexOf(".")))) {
                return scriptType.realName;
            }
        }
        return "";
    }

    /**
     * 根据文件后缀名称获取文件展示类型
     *
     * @param fileName 文件名称
     * @return 文件类型
     */
    public static String getScriptShowType(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return "";
        }
        ScriptType[] scriptTypes = values();
        for (ScriptType scriptType : scriptTypes) {
            if (scriptType.suffixName.equalsIgnoreCase(fileName.substring(fileName.lastIndexOf(".")))) {
                return scriptType.showName;
            }
        }
        return "";
    }

    public String getRealName() {
        return realName;
    }

    public String getShowName() {
        return showName;
    }
}

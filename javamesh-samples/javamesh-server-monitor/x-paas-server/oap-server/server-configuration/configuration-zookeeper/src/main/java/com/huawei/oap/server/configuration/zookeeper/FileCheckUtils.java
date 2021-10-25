/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.oap.server.configuration.zookeeper;

import org.apache.skywalking.apm.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.Normalizer;

/**
 * 文件校验
 *
 * @author zhouss
 * @since 2021-05-06
 **/
public class FileCheckUtils {
    // 跨目录危险字符
    private static final String[] CROSS_DIRECTORY_ATTACK =
            new String[] {"~/", "../", "./", "..\\", ".\\", ".''", "''."};

    /**
     * 安全地创建新{@code FileInputStream}实例，相当于{@code new FileInputStream(File file)}
     *
     * @param file 抽象文件名
     * @return FileInputStream 新的{@code FileInputStream}实例
     * @throws RuntimeException 如果文件不存在，无权限，或文件名{@code file}不合法
     * @throws FileNotFoundException 如果文件不存在，无权限
     */
    public static FileInputStream getFileInputStream(File file) throws FileNotFoundException {
        if (isSecurityFileName(file.getPath())) {
            return new FileInputStream(file.getPath());
        }
        throw new IllegalArgumentException("invalid character in path");
    }

    private static boolean isSecurityFileName(String fileName) {
        final String normalizedFileName = normalizeString(fileName);
        if (StringUtil.isEmpty(normalizedFileName)) {
            return true;
        }
        // 跨目录攻击校验
        for (String aCheckStr : CROSS_DIRECTORY_ATTACK) {
            if (normalizedFileName.contains(aCheckStr)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 归一化字符串
     *
     * @param str 需要归一化的字符串
     * @return 归一化的字符串
     */
    private static String normalizeString(String str) {
        if (StringUtil.isNotEmpty(str)) {
            return Normalizer.normalize(str, Normalizer.Form.NFKC);
        }
        return str;
    }
}

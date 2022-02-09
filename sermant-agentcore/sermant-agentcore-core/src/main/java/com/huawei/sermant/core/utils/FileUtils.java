/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.utils;

import com.huawei.sermant.core.common.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * io工具类
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-18
 */
public class FileUtils {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 缓冲区大小
     */
    private static final int BUFFER_SIZE = 1024 * 16;

    private FileUtils() {
    }

    /**
     * 确保一个文件或文件夹的父目录存在
     *
     * @param file 文件或文件夹
     * @return 是否存在或创建成功
     */
    public static boolean createParentDir(File file) {
        final File parentDir = file.getParentFile();
        return parentDir.exists() || parentDir.mkdirs();
    }

    /**
     * 将源文件拷贝到目标路径
     *
     * @param sourceFile 源文件
     * @param targetFile 目标文件
     * @throws IOException 拷贝失败
     */
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(sourceFile);
            outputStream = new FileOutputStream(targetFile);
            final byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = inputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, length);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                    LOGGER.warning("Unexpected exception occurs. ");
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                    LOGGER.warning("Unexpected exception occurs. ");
                }
            }
        }
    }

    /**
     * 拷贝文件夹下所有文件
     *
     * @param sourceFile 源文件夹
     * @param targetPath 目标文件夹
     * @param isCover    是否覆盖
     * @throws IOException 拷贝失败
     */
    public static void copyAllFiles(File sourceFile, String targetPath, boolean isCover) throws IOException {
        if (sourceFile.isFile()) {
            final File targetFile = new File(targetPath);
            if ((isCover || !targetFile.exists()) && createParentDir(targetFile)) {
                copyFile(sourceFile, targetFile);
            }
        } else {
            final File[] subFiles = sourceFile.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    copyAllFiles(subFile, targetPath + File.separatorChar + subFile.getName(), isCover);
                }
            }
        }
    }

    /**
     * 删除文件夹及其内部所有文件
     *
     * @param file 文件或文件夹
     * @return 是否全部删除成功
     */
    public static boolean deleteDirs(File file) {
        if (!file.exists()) {
            return true;
        }
        if (file.isDirectory()) {
            final File[] subFiles = file.listFiles();
            if (subFiles != null) {
                boolean isDeleteSucceed = true;
                for (File subFile : subFiles) {
                    isDeleteSucceed &= deleteDirs(subFile);
                }
                return isDeleteSucceed && file.delete();
            }
        }
        return file.delete();
    }

    /**
     * 通过通配符的方式检索子文件
     *
     * @param dir   文件夹
     * @param wcStr 通配符模式，允许','拼接多个
     * @return 子文件集
     */
    public static File[] getChildrenByWildcard(File dir, String wcStr) {
        if (!dir.exists() || !dir.isDirectory()) {
            return new File[0];
        }
        final String[] wcs = wcStr.split(",");
        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                for (String wc : wcs) {
                    if (StringUtils.isWildcardMatch(name, wc)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }
}

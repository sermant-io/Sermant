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

package com.huawei.hercules.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 功能描述：下载工具
 *
 * @author z30009938
 * @since 2021-10-18
 */
public abstract class DownloadUtils {
    /**
     * 定义缓存区大小
     */
    private static final int DOWNLOAD_BUFFER_SIZE = 4096;

    /**
     * 定义日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadUtils.class);

    /**
     * 把输入流的内容通过文件的形式，下载给前端
     *
     * @param response 请求相应实体
     * @param inputStream 数据输入流
     * @param fileName 文件名称
     * @return 执行成功返回true，执行失败返回false
     */
    public static boolean downloadFile(HttpServletResponse response, InputStream inputStream, String fileName) {
        if (response == null || inputStream == null || StringUtils.isEmpty(fileName)) {
            return false;
        }
        byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
        OutputStream toClient = null;
        try {
            response.reset();
            response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setContentType("application/octet-stream");
            toClient = new BufferedOutputStream(response.getOutputStream());
            int readLength;
            while (((readLength = inputStream.read(buffer)) != -1)) {
                toClient.write(buffer, 0, readLength);
            }
            toClient.flush();
            return true;
        } catch (IOException e) {
            LOGGER.error("Download agent fail.", e);
            return false;
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(toClient);
        }
    }
}

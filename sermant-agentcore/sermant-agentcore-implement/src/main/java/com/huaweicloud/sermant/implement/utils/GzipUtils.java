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

package com.huaweicloud.sermant.implement.utils;

import com.huaweicloud.sermant.core.common.LoggerFactory;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 压缩和解压缩工具类
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-03-26
 */
public class GzipUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    // 缓冲区大小
    private static final int BUFFER = 1024;

    private static final int FLAG = -1;

    private GzipUtils() {
    }

    /**
     * 数据压缩
     *
     * @param data 待压缩的数据
     * @return 压缩完成的数据
     */
    public static byte[] compress(byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 压缩
        compress(bais, baos);
        byte[] output = baos.toByteArray();
        return getBytes(output, bais, baos);
    }

    /**
     * 数据压缩
     *
     * @param is 输入流
     * @param os 输出流
     */
    public static void compress(InputStream is, OutputStream os) {
        try (GZIPOutputStream gos = new GZIPOutputStream(os)) {
            int count;
            byte[] data = new byte[BUFFER];
            while ((count = is.read(data, 0, BUFFER)) != FLAG) {
                gos.write(data, 0, count);
            }
            gos.finish();
            gos.flush();
        } catch (IOException e) {
            LOGGER.severe(
                String.format(Locale.ROOT, "Exception occurs when compress. Exception info: [%s]}", e.getMessage()));
        }
    }

    /**
     * 数据解压缩
     *
     * @param data 待解压数据
     * @return 解压完成的数据
     */
    public static byte[] decompress(byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 解压缩
        decompress(bais, baos);
        return getBytes(baos.toByteArray(), bais, baos);
    }

    /**
     * 数据解压缩
     *
     * @param is 输入流
     * @param os 输出流
     */
    public static void decompress(InputStream is, OutputStream os) {
        try (GZIPInputStream gis = new GZIPInputStream(is)) {
            int count;
            byte[] data = new byte[BUFFER];
            while ((count = gis.read(data, 0, BUFFER)) != FLAG) {
                os.write(data, 0, count);
            }
        } catch (IOException e) {
            LOGGER.severe(
                String.format(Locale.ROOT, "Exception occurs when decompress. Exception info: [%s]}", e.getMessage()));
        }
    }

    private static byte[] getBytes(byte[] data, ByteArrayInputStream bais, ByteArrayOutputStream baos) {
        try {
            baos.flush();
        } catch (IOException e) {
            LOGGER.severe(
                String.format(Locale.ROOT, "Exception occurs when getBytes. Exception info: [%s]}", e.getMessage()));
        } finally {
            IOUtils.closeQuietly(bais);
            IOUtils.closeQuietly(baos);
        }
        return data;
    }
}

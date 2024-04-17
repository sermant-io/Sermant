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

package com.huaweicloud.sermant.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Compression and decompression tools
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-03-26
 */
public class GzipUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(GzipUtils.class);

    // Buffer size
    private static final int BUFFER = 1024;

    private GzipUtils() {
    }

    /**
     * Data compaction
     *
     * @param data Data to be compressed
     * @return Compressed data
     */
    public static byte[] compress(byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // compress
            compress(bais, baos);
            byte[] output = baos.toByteArray();
            baos.flush();
            return output;
        } catch (IOException e) {
            LOGGER.error("Exception occurs when compress or close IOStream. Exception info: {}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Data compression
     *
     * @param is input stream
     * @param os output stream
     */
    public static void compress(InputStream is, OutputStream os) {
        GZIPOutputStream gos = null;
        try {
            gos = new GZIPOutputStream(os);
            int count;
            byte[] data = new byte[BUFFER];
            while ((count = is.read(data, 0, BUFFER)) != -1) {
                gos.write(data, 0, count);
            }
            gos.finish();
            gos.flush();
        } catch (IOException e) {
            LOGGER.error("Exception occurs when compress. Exception info: {}", e.getMessage());
        } finally {
            try {
                if (gos != null) {
                    gos.close();
                }
            } catch (IOException e) {
                LOGGER.error("Exception occurs when close IOStream, Exception info: {}", e.getMessage());
            }
        }
    }

    /**
     * Data decompression
     *
     * @param data Data to be decompressed
     * @return Decompressed data
     */
    public static byte[] decompress(byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // decompress
        decompress(bais, baos);
        byte[] newData = baos.toByteArray();
        try {
            baos.flush();
        } catch (IOException e) {
            LOGGER.error("Exception occurs when decompress. Exception info: {}", e.getMessage());
        } finally {
            try {
                baos.close();
                bais.close();
            } catch (IOException e) {
                LOGGER.error("Exception occurs when close IOStream. Exception info: {}", e.getMessage());
            }
        }
        return newData;
    }

    /**
     * Data decompression
     *
     * @param is input stream
     * @param os output stream
     */
    public static void decompress(InputStream is, OutputStream os) {
        GZIPInputStream gis = null;
        try {
            gis = new GZIPInputStream(is);
            int count;
            byte[] data = new byte[BUFFER];
            while ((count = gis.read(data, 0, BUFFER)) != -1) {
                os.write(data, 0, count);
            }
        } catch (IOException e) {
            LOGGER.error("Exception occurs when decompress. Exception info: {}", e.getMessage());
        } finally {
            try {
                if (gis != null) {
                    gis.close();
                }
            } catch (IOException e) {
                LOGGER.error("Exception occurs when close IOStream. Exception info: {}", e.getMessage());
            }
        }
    }
}

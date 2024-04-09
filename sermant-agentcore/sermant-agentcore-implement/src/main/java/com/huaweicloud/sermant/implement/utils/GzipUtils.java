/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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
 * Compression and decompression tools
 *
 * @author lilai
 * @version 0.0.1
 * @since 2021-03-26
 */
public class GzipUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    // Buffer size
    private static final int BUFFER = 1024;

    private static final int FLAG = -1;

    private GzipUtils() {
    }

    /**
     * Data compaction
     *
     * @param data Data to be compressed
     * @return Compressed data
     */
    public static byte[] compress(byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // compress
        compress(bais, baos);
        byte[] output = baos.toByteArray();
        return getBytes(output, bais, baos);
    }

    /**
     * Data compression
     *
     * @param is input stream
     * @param os output stream
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
                    String.format(Locale.ROOT, "Exception occurs when compress. Exception info: [%s]}",
                            e.getMessage()));
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

        // uncompress
        decompress(bais, baos);
        return getBytes(baos.toByteArray(), bais, baos);
    }

    /**
     * Data decompression
     *
     * @param is input stream
     * @param os output stream
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
                    String.format(Locale.ROOT, "Exception occurs when decompress. Exception info: [%s]}",
                            e.getMessage()));
        }
    }

    private static byte[] getBytes(byte[] data, ByteArrayInputStream bais, ByteArrayOutputStream baos) {
        try {
            baos.flush();
        } catch (IOException e) {
            LOGGER.severe(
                    String.format(Locale.ROOT, "Exception occurs when getBytes. Exception info: [%s]}",
                            e.getMessage()));
        } finally {
            IOUtils.closeQuietly(bais);
            IOUtils.closeQuietly(baos);
        }
        return data;
    }
}

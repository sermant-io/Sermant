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

package io.sermant.core.utils;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.agent.config.AgentConfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * io tools
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-18
 */
public class FileUtils {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String[] INVALID_SYMBOL = {"../", "..\\"};

    private static final String AGENT_PATH = new File(new File(FileUtils.class.getProtectionDomain().getCodeSource()
            .getLocation().getPath()).getParent()).getParent();

    /**
     * file name of unmatched class name
     */
    private static final String DEFAULT_OUTPUT_CLASS_NAME_FILE = "unmatched_class_name.txt";

    /**
     * unmatched class cache
     */
    private static final Map<String, String> UNMATCHED_CLASS_CACHE = new ConcurrentHashMap<>();

    /**
     * buffer size
     */
    private static final int BUFFER_SIZE = 1024 * 16;

    private FileUtils() {
    }

    /**
     * Gets the absolute path to the folder of sermant-agent-x.x.x/agent
     *
     * @return path
     */
    public static String getAgentPath() {
        return AGENT_PATH;
    }

    /**
     * Check file path
     *
     * @param path input path
     * @return fixed path
     */
    public static String validatePath(String path) {
        if (!path.startsWith(AGENT_PATH)) {
            return "";
        }

        String fixPath = path;
        for (String symbol : INVALID_SYMBOL) {
            fixPath = fixPath.replace(symbol, "");
        }
        return fixPath;
    }

    /**
     * Ensure that the parent directory of a file or folder exists
     *
     * @param file file
     * @return create result
     */
    public static boolean createParentDir(File file) {
        final File parentDir = file.getParentFile();
        return parentDir.exists() || parentDir.mkdirs();
    }

    /**
     * Copy the source file to the destination path
     *
     * @param sourceFile source file
     * @param targetFile target file
     * @throws IOException copy exception
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
     * Copy all files in the folder
     *
     * @param sourceFile source file
     * @param targetPath target path
     * @param isCover is cover
     * @throws IOException opy exception
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
     * Delete the folder and all files inside it
     *
     * @param file file
     * @return delete result
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
     * Retrieve sub files by wildcard
     *
     * @param dir directory
     * @param wcStr Wildcard mode, allowing ',' to concatenate multiple
     * @return sub file set
     */
    public static File[] getChildrenByWildcard(File dir, String wcStr) {
        if (!dir.exists() || !dir.isDirectory()) {
            return new File[0];
        }
        final String[] wcs = wcStr.split(",");
        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return matchFileByWildcard(name, wcs);
            }
        });
    }

    /**
     * read file and convert it to string
     *
     * @param filePath file path
     * @return String
     */
    public static String readFileToString(String filePath) {
        // Read all bytes from the file at once
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "cannot read file content, please check the path");
            return StringUtils.EMPTY;
        }

        // Convert the bytes to a String using UTF-8 encoding
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static boolean matchFileByWildcard(String name, String[] wcs) {
        for (String wc : wcs) {
            if (StringUtils.isWildcardMatch(name, wc)) {
                return true;
            }
        }
        return false;
    }

    /**
     * add unmatched class for enhancement to cache
     *
     * @param className class name
     */
    public static void addToUnmatchedClassCache(String className) {
        UNMATCHED_CLASS_CACHE.putIfAbsent(className, className);
    }

    /**
     * output file
     */
    public static void writeUnmatchedClassNameToFile() {
        try (BufferedWriter classNameWriter = new BufferedWriter(new FileWriter(buildUnmatchedFileString()))) {
            for (String className : UNMATCHED_CLASS_CACHE.keySet()) {
                classNameWriter.write(className);
                classNameWriter.newLine();
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Fail to write class name to file. ", e);
        }
    }

    /**
     * load unmatched file name to cache
     */
    public static void readUnmatchedClassNameFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(buildUnmatchedFileString()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                UNMATCHED_CLASS_CACHE.putIfAbsent(line, line);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Fail to read class name from file: " + e.getMessage());
        }
    }

    public static Map<String, String> getUnMatchedClassCache() {
        return UNMATCHED_CLASS_CACHE;
    }

    private static String buildUnmatchedFileString() {
        AgentConfig config = ConfigManager.getConfig(AgentConfig.class);
        String preFilterPath = config.getPreFilterPath();
        if (StringUtils.isEmpty(preFilterPath)) {
            preFilterPath = AGENT_PATH;
        }
        String preFilterFile = config.getPreFilterFile();
        if (StringUtils.isEmpty(preFilterFile)) {
            preFilterFile = DEFAULT_OUTPUT_CLASS_NAME_FILE;
        }
        return preFilterPath + File.separatorChar + preFilterFile;
    }
}

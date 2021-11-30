package com.huawei.apm.core.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import com.huawei.apm.core.common.LoggerFactory;

public class FileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    public static String getCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException ignored) {
            LOGGER.warning("Explain " + file + "'s canonical path failed. ");
            return null;
        }
    }

    public static URL toURL(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException ignored) {
            LOGGER.warning("Explain " + file + "'s url failed. ");
            return null;
        }
    }

    public static JarFile toJarFile(File file) {
        try {
            return new JarFile(file);
        } catch (IOException ignored) {
            LOGGER.warning("Transfer " + file + " to jar file failed. ");
            return null;
        }
    }
}

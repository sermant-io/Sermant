package com.huawei.javamesh.core.lubanops.bootstrap.utils;

import com.huawei.javamesh.core.lubanops.bootstrap.log.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Properties;
import java.util.logging.Level;

/**
 * 文件操作工具类 <br>
 * @author zWX482523
 * @since 2018年3月1日
 */
public class FileUtils {

    /**
     * 文件内容写入 <br>
     *
     * @param filePath
     * @param content
     * @return
     * @author zWX482523
     * @since 2018年3月1日
     */
    public static boolean writeFile(String filePath, String content) {
        FileWriter writer = null;
        String safePath = InputSafetyChecker.getSafePath(filePath);
        try {
            File file = new File(safePath);
            if (!file.getParentFile().exists()) {
                // 如果目标文件所在的目录不存在，则创建父目录
                boolean result = file.getParentFile().mkdirs();
                if (!result) {
                    return false;
                }
            }
            if (!file.exists()) {
                boolean result = file.createNewFile();
                if (!result) {
                    return false;
                }
            }
            writer = new FileWriter(safePath);
            writer.write(content);
        } catch (IOException e) {
            LogFactory.getLogger().log(Level.SEVERE, LogForgingUtil.replace(filePath) + "文件写入失败", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
        return true;
    }

    /**
     * 读取指定文件内容 <br>
     *
     * @param filePath
     * @return
     * @author zWX482523
     * @since 2018年3月1日
     */
    public static String readFile(String filePath) {
        StringBuilder content = new StringBuilder();
        FileReader reader = null;
        int c = 0;
        try {
            String safePath = InputSafetyChecker.getSafePath(filePath);
            reader = new FileReader(safePath);
            c = reader.read();
            while (c != -1) {
                content.append((char) c);
                c = reader.read();
            }
        } catch (IOException e) {
            LogFactory.getLogger().log(Level.SEVERE, LogForgingUtil.replace(filePath) + "文件读取失败", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        return content.toString();
    }

    /**
     * 读取指定文件内容 <br>
     * @param filePath
     * @return
     * @author zWX482523
     * @since 2018年3月1日
     */
    public static Properties readFilePropertyByPath(String filePath) {
        String safePath = InputSafetyChecker.getSafePath(filePath);
        Properties properties = new Properties();
        FileInputStream fileIs = null;
        try {
            File file = new File(safePath);
            if (file.exists()) {
                fileIs = new FileInputStream(safePath);
                properties.load(fileIs);
                return properties;
            }
        } catch (Exception e) {
            LogFactory.getLogger().log(Level.WARNING, filePath + "配置文件读取失败！");
        } finally {
            if (fileIs != null) {
                try {
                    fileIs.close();
                } catch (IOException e) {
                }
            }
        }
        return properties;
    }

    public static File getInterceptorFile(ClassLoader classLoader, String interceptorName)
            throws ClassNotFoundException {
        ProtectionDomain pd = classLoader.loadClass(interceptorName).getProtectionDomain();
        CodeSource cs = pd.getCodeSource();
        String jarPath = cs.getLocation().getPath();
        return new File(jarPath);
    }

}

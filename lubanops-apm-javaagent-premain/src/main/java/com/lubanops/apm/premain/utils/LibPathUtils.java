package com.lubanops.apm.premain.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Level;

import com.lubanops.apm.bootstrap.log.LogFactory;

/**
 * 获取javaagent依赖的jar
 *
 * @author
 */
public class LibPathUtils {

    private static final String AGENT_JAR_FILE_NAME = "apm-javaagent.jar";

    private static String agentPath = "";

    /**
     * 获取agent中相关包 <br>
     *
     * @return
     * @author
     */
    public static List<URL> getLibUrl() {
        List<URL> libURLList = new ArrayList<URL>();
        try {
            ProtectionDomain pd = LibPathUtils.class.getProtectionDomain();
            CodeSource cs = pd.getCodeSource();
            String jarPath = cs.getLocation().getPath();
            String agentPath = jarPath.substring(0, jarPath.lastIndexOf(AGENT_JAR_FILE_NAME));
            String libPath = agentPath + File.separator + "lib";
            libURLList.addAll(getLibUrl(libPath));
            String corePath = agentPath + File.separator + "core";
            libURLList.addAll(getTransformerLibUrl(corePath));
        } catch (Exception e) {
        }
        return libURLList;
    }

    public static String getAgentPath() {
        String agentPath = LibPathUtils.agentPath;
        if (null != agentPath && agentPath.length() > 0) {
            return agentPath;
        }
        try {
            ProtectionDomain pd = LibPathUtils.class.getProtectionDomain();
            CodeSource cs = pd.getCodeSource();
            String jarPath = cs.getLocation().getPath();
            agentPath = jarPath.substring(0, jarPath.lastIndexOf(AGENT_JAR_FILE_NAME));
            LibPathUtils.agentPath = agentPath;
        } catch (Exception e) {
        }
        return agentPath;
    }

    public static List<URL> getLibUrl(String path) {
        List<URL> jarURLList = new ArrayList<URL>();
        try {
            File libDir = new File(path);
            File[] files = libDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    jarURLList.add(file.toURI().toURL());
                }
            }
        } catch (Exception e) {
        }
        return jarURLList;
    }

    public static List<URL> getTransformerLibUrl(String path) {
        List<URL> jarURLList = new ArrayList<URL>();
        try {
            File libDir = new File(path);
            File[] files = libDir.listFiles();
            if (files != null) {
                File transformerFile = null;
                for (File file : files) {
                    if (transformerFile == null || (!transformerFile.getName()
                        .substring(0, transformerFile.getName().lastIndexOf("-"))
                        .equals(file.getName().substring(0, file.getName().lastIndexOf("-"))))) {
                        transformerFile = file;
                    } else {
                        transformerFile = compareFileName(file, transformerFile);
                    }
                    if (transformerFile != null) {
                        LogFactory.getLogger().log(Level.INFO,
                            String.format("[CLASS LOAD]jar[%s] loaded.", transformerFile.toURI().toURL()));
                        jarURLList.add(transformerFile.toURI().toURL());
                    }
                }
            }
        } catch (Exception e) {
        }
        return jarURLList;
    }

    /**
     * 比较包的版本号 取最新的版本包
     *
     * @param file
     * @param transformerFile
     * @return
     */
    private static File compareFileName(File file, File transformerFile) {
        String fileName = file.getName();
        String transformerFileName = transformerFile.getName();
        String javaagentVersion = fileName.substring(fileName.lastIndexOf("-") + 1, fileName.lastIndexOf(".jar"));
        String newJavaagentVersion = transformerFileName.substring(fileName.lastIndexOf("-") + 1,
            transformerFileName.lastIndexOf(".jar"));
        // 注意此处为正则匹配，不能用"."
        String[] versionArray1 = javaagentVersion.split("\\.");
        String[] versionArray2 = newJavaagentVersion.split("\\.");
        int idx = 0;
        // 取最小长度值
        int minLength = Math.min(versionArray1.length, versionArray2.length);
        int diff = 0;

        //  先比较长度再比较字符
        while (idx < minLength && (diff = versionArray1[idx].length() - versionArray2[idx].length()) == 0
            && (diff = versionArray1[idx].compareTo(versionArray2[idx])) == 0) {
            ++idx;
        }
        // 如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大；
        diff = (diff != 0) ? diff : versionArray1.length - versionArray2.length;
        if (diff > 0) {
            return file;
        } else {
            return transformerFile;
        }
    }

    public static List<JarFile> getPluginJar() {
        List<JarFile> list = new ArrayList<JarFile>();
        try {
            String bootPath = getAgentPath() + "plugins";
            getFiles(list, bootPath);
        } catch (Exception e) {
        }
        return list;
    }

    private static void getFiles(List<JarFile> list, String bootPath) throws IOException {
        File libDir = new File(bootPath);
        File[] files = libDir.listFiles();
        if (files != null) {
            for (File file : files) {
                list.add(new JarFile(file));
            }
        }
    }

    public static List<JarFile> getBootstrapJar() {
        List<JarFile> list = new ArrayList<JarFile>();
        try {
            String bootPath = getAgentPath() + File.separator + "boot";
            getFiles(list, bootPath);
        } catch (Exception e) {
        }
        return list;
    }

    public static List<JarFile> getPluginJars() {
        List<JarFile> list = new ArrayList<JarFile>();
        try {
            String bootPath = getAgentPath() + File.separator + "plugins";
            getFiles(list, bootPath);
        } catch (Exception e) {
        }
        return list;
    }

    public static List<File> getPluginFiles() {
        List<File> list = new ArrayList<File>();
        try {
            String bootPath = getAgentPath() + File.separator + "plugins";
            File libDir = new File(bootPath);
            File[] files = libDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    list.add(file);
                }
            }
        } catch (Exception e) {
        }
        return list;
    }

    public static String getBootstrapJarPath() {
        return getAgentPath() + File.separator + "boot";
    }

    public static String getPluginsJarPath() {
        return getAgentPath() + File.separator + "plugins";
    }

    public static File getSystemJarFile() {
        try {
            String bootPath = getAgentPath() + File.separator + "system";
            File libDir = new File(bootPath);
            File[] files = libDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    return file;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

}

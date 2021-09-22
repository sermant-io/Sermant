package com.lubanops.apm.bootstrap.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lubanops.apm.bootstrap.log.LogFactory;

public class Util {

    private static final char[] HEX_DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static <T> boolean objectEqual(T s1, T s2) {
        return s1 == null ? s2 == null : s1.equals(s2);
    }

    /*
     * 将字符串写入文件
     */
    public static void writeToFile(String path, String content) throws IOException {
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(path));
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bufferedOutputStream.write(content.getBytes("utf-8"));
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (bufferedOutputStream != null) {
                bufferedOutputStream.close();
            }
        }
    }

    public static String getHomePath() {
        String homePath = (String) System.getProperties().get("user.home");
        if (homePath == null) {
            homePath = "/tmp";
        }
        return homePath;
    }

    public static String getMD5String(String value) {
        if (value == null) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("md5");
            messageDigest.update(value.getBytes("utf8"));
            return getFormattedText(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        for (int j = 0; j < len; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }

    public static String readDubboAbstractInvokerInterceptorCode() {
        return readClasspathFile("sentry.dubbo.AbstractInvoker.txt");
    }

    public static String readClasspathFile(String fileName) {

        BufferedReader br = null;
        try {
            StringBuilder sb = new StringBuilder();
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
            br = new BufferedReader(new InputStreamReader(is, "utf-8"));

            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append("\n");
            }

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("failed to read file:" + fileName, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {

                }
            }

        }

    }

    public static String getInstanceNameByUserDir() {
        String userDir = System.getProperty("nuwa.home");
        if (userDir == null || "".equals(userDir)) {
            userDir = System.getProperty("catalina.home");
        }
        if (userDir == null || "".equals(userDir)) {
            userDir = System.getProperty("user.dir");
        }
        userDir = filterUserDir(userDir);
        String instanceName = Util.getMD5String(userDir).substring(0, 3);
        instanceName = checkInstanceNameByUserDir(userDir, instanceName);
        return instanceName;
    }

    public static String filterUserDir(String userDir) {
        String reg = "\\d{4,}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(userDir);
        if (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            userDir = userDir.substring(0, start) + userDir.substring(end);
            return filterUserDir(userDir);
        } else {
            return userDir;
        }
    }

    public static String checkInstanceNameByUserDir(String userDir, String instanceName) {
        String filePath = "/apm/instances/" + instanceName + "/user_dir.txt";
        String oldUseDir = FileUtils.readFile(filePath);
        if (oldUseDir == null || "".equals(oldUseDir)) {
            FileUtils.writeFile(filePath, userDir);
            return instanceName;
        } else if (!oldUseDir.equals(userDir)) {
            instanceName = Util.getMD5String(userDir).substring(0, instanceName.length() + 1);
            return checkInstanceNameByUserDir(userDir, instanceName);
        } else {
            return instanceName;
        }
    }

    public static String getJarVersionFromProtectionDomain(ProtectionDomain protectionDomain) {
        String path = null;
        try {
            path = protectionDomain.getCodeSource().getLocation().getPath();
        } catch (Exception e) {
            // ignore
        }
        return path == null ? "unknown" : Util.getJarFileName(path);
    }

    /**
     * 得到一个jar文件的文件名
     * 比如"/C:/Users/hzyefeng/.m2/repository/com/alibaba/dubbo/2.5.3/dubbo-2.5.3.jar"
     * 比如"/C:/Users/hzyefeng/.m2/repository/com/alibaba/dubbo/2.5.3/dubbo-2.5.3.jar!/"
     * 最后获取 dubbo-5.3.2.jar
     *
     * @param path
     * @return
     */
    private static String getJarFileName(String path) {
        int i = path.lastIndexOf("/");
        if (i < 0) {
            i = path.lastIndexOf("\\");
            if (path.endsWith("\\")) {
                i = path.lastIndexOf("\\", path.lastIndexOf("\\") - 1);
            }
        } else if (path.endsWith("/")) {
            i = path.lastIndexOf("/", path.lastIndexOf("/") - 1);
        }

        if (i > 0) {
            String jarFile = path.substring(i + 1);
            // "XXX-Version.jar!/" should be modified to "XXX-Version.jar"
            if (jarFile.endsWith("!/")) {
                jarFile = jarFile.replace("!/", "");
            }
            return jarFile;
        } else {
            return path;
        }
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            if (clazz != null) {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
            }
        } catch (NoSuchMethodException e) {
            method = getMethod(clazz.getSuperclass(), methodName, parameterTypes);
        } catch (SecurityException e) {
            LogFactory.getLogger().log(Level.WARNING, "Failed to get method [" + methodName + "], " + e.getMessage(), e);
        }
        return method;
    }
}

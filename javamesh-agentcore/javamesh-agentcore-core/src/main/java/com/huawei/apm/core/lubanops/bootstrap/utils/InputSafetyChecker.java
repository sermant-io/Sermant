package com.huawei.apm.core.lubanops.bootstrap.utils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;

/**
 * 输入字符串校验
 */
public class InputSafetyChecker {

    public static final String CHARACTER_REGEX = "^[A-Za-z_]+$";

    public static final Pattern CHARACTER_PATTERN = Pattern.compile(CHARACTER_REGEX);

    public static final String CHARACTER_NUMBER_REGEX = "^[A-Za-z0-9_]+$";

    public static final Pattern CHARACTER_NUMBER_PATTERN = Pattern.compile(CHARACTER_NUMBER_REGEX);

    public static final String CHARACTER_NUMBER_HYPHEN_REGEX = "^[A-Za-z0-9_\\-]+$";

    public static final Pattern CHARACTER_NUMBER_HYPHEN_PATTERN = Pattern.compile(CHARACTER_NUMBER_HYPHEN_REGEX);

    public static final Pattern DEFAULT_PATTERN = CHARACTER_NUMBER_HYPHEN_PATTERN;

    /**
     * 文件路径校验白名单
     *
     * @see <a href=
     * "http://3ms.huawei.com/hi/group/2027375/thread_7566637.html?mapId=9340983">编程规范第二期
     * CodeDex问题-HW_CSPL_JAVA_Path_Manipulation分析</a>
     */
    private static final String PATH_WHITE_LIST
        = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890-=[];\\',./ ~!@#$%^&*()_+\"{}|:<>?";

    private InputSafetyChecker() {
    }

    /**
     * 采用默认pattern校验输入
     *
     * @param input
     * @param errorMessage
     * @throws IllegalArgumentException
     */
    public static void check(String input, String errorMessage) throws IllegalArgumentException {
        if (!DEFAULT_PATTERN.matcher(input).matches()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * 按照输入的pattern校验输入
     *
     * @param pattern
     * @param input
     * @param errorMessage
     * @throws IllegalArgumentException
     */
    public static void check(Pattern pattern, String input, String errorMessage) throws IllegalArgumentException {
        if (!pattern.matcher(input).matches()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * 检验全文件路径中的字符是否都是合法路径
     *
     * @param filePath
     * @return
     */
    private static String checkFile(final String filePath) {
        if (null == filePath) {
            return null;
        }
        final StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < filePath.length(); i++) {
            for (int j = 0; j < PATH_WHITE_LIST.length(); j++) {
                if (filePath.charAt(i) == PATH_WHITE_LIST.charAt(j)) {
                    stringBuffer.append(PATH_WHITE_LIST.charAt(j));
                    break;
                }
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 校验文件是否合法路径，不合法直接抛出异常
     *
     * @param filePath
     * @return
     */
    public static String getSafePath(String filePath) {
        File file = new File(filePath);
        String safePath = null;
        try {
            // 将路径转换为标准路径，替换掉相对路径
            safePath = file.getCanonicalPath();
        } catch (IOException e) {
            LogFactory.getLogger().severe("getSafePath IOException");
        }
        return checkFile(safePath);
    }

}

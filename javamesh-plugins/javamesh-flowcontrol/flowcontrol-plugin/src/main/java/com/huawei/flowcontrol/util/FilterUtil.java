/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.util;

import com.alibaba.csp.sentinel.util.StringUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * 过滤工具类
 *
 * @author zhanghu
 * @since 2021-03-29
 */
public final class FilterUtil {
    private static final String PATH_SPLIT = "/";
    private static final int NEGATIVE_ONE = -1;

    private FilterUtil() {
    }

    /**
     * 过滤目标对象
     *
     * @param request HttpServletRequest对象
     * @return 过滤后的字符串
     */
    public static String filterTarget(HttpServletRequest request) {
        String pathInfo = getResourcePath(request);
        if (!pathInfo.startsWith(PATH_SPLIT)) {
            pathInfo = PATH_SPLIT + pathInfo;
        }

        if (PATH_SPLIT.equals(pathInfo)) {
            return pathInfo;
        }

        // Note: pathInfo should be converted to camelCase style.
        int lastSlashIndex = pathInfo.lastIndexOf(PATH_SPLIT);

        if (lastSlashIndex >= 0) {
            pathInfo = pathInfo.substring(0, lastSlashIndex) + PATH_SPLIT
                    + StringUtil.trim(pathInfo.substring(lastSlashIndex + 1));
        } else {
            pathInfo = PATH_SPLIT + StringUtil.trim(pathInfo);
        }

        return pathInfo;
    }

    private static String getResourcePath(HttpServletRequest request) {
        String pathInfo = normalizeAbsolutePath(request.getPathInfo(), false);
        String servletPath = normalizeAbsolutePath(request.getServletPath(), pathInfo.length() != 0);

        return servletPath + pathInfo;
    }

    private static String normalizeAbsolutePath(String path, boolean removeTrailingSlash) throws IllegalStateException {
        return normalizePath(path, true, false, removeTrailingSlash);
    }

    private static String normalizePath(String path, boolean forceAbsolute, boolean forceRelative,
                                    boolean removeTrailingSlash) throws IllegalStateException {
        char[] pathChars = StringUtil.trimToEmpty(path).toCharArray();
        int length = pathChars.length;

        // Check path and slash.
        boolean startsWithSlash = false;
        boolean endsWithSlash = false;

        if (length > 0) {
            char firstChar = pathChars[0];
            char lastChar = pathChars[length - 1];

            startsWithSlash = firstChar == PATH_SPLIT.charAt(0) || firstChar == '\\';
            endsWithSlash = lastChar == PATH_SPLIT.charAt(0) || lastChar == '\\';
        }

        StringBuilder buf = new StringBuilder(length);
        boolean isAbsolutePath = forceAbsolute || !forceRelative && startsWithSlash;
        int index = startsWithSlash ? 0 : NEGATIVE_ONE;
        int level = 0;

        if (isAbsolutePath) {
            buf.append(PATH_SPLIT);
        }

        while (index < length) {
            index = indexOfSlash(pathChars, index + 1, false);

            if (index == length) {
                break;
            }

            int nextSlashIndex = indexOfSlash(pathChars, index, true);

            String element = new String(pathChars, index, nextSlashIndex - index);
            index = nextSlashIndex;

            if (".".equals(element)) {
                continue;
            }

            if ("..".equals(element)) {
                level = getLevel(path, pathChars, buf, isAbsolutePath, level);
                continue;
            }

            pathChars[level++] = (char) buf.length();
            buf.append(element).append(PATH_SPLIT);
        }

        if (buf.length() > 0 && (!endsWithSlash || removeTrailingSlash)) {
            buf.setLength(buf.length() - 1);
        }
        return buf.toString();
    }

    private static int getLevel(String path, char[] pathChars,
                            StringBuilder buf, boolean isAbsolutePath, int level) {
        int levelValue = level;
        if (levelValue == 0) {
            if (isAbsolutePath) {
                throw new IllegalStateException(path);
            } else {
                buf.append("..").append(PATH_SPLIT);
            }
        } else {
            buf.setLength(pathChars[--levelValue]);
        }
        return levelValue;
    }

    private static int indexOfSlash(char[] chars, int beginIndex, boolean slash) {
        int index = beginIndex;

        for (; index < chars.length; index++) {
            char ch = chars[index];

            if (slash) {
                if (ch == PATH_SPLIT.charAt(0) || ch == '\\') {
                    break; // if a slash
                }
            } else {
                if (ch != PATH_SPLIT.charAt(0) && ch != '\\') {
                    break; // if not a slash
                }
            }
        }
        return index;
    }
}

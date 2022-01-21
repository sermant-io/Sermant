/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.util;

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
     * @param rawPathInfo 路径信息
     * @param rawServletPath 子路径
     * @return 过滤后的字符串
     */
    public static String filterTarget(String rawPathInfo, String rawServletPath) {
        String pathInfo = getResourcePath(rawPathInfo, rawServletPath);
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
                    + StringUtils.trim(pathInfo.substring(lastSlashIndex + 1));
        } else {
            pathInfo = PATH_SPLIT + StringUtils.trim(pathInfo);
        }

        return pathInfo;
    }

    private static String getResourcePath(String rawPathInfo, String rawServletPath) {
        String pathInfo = normalizeAbsolutePath(rawPathInfo, false);
        String servletPath = normalizeAbsolutePath(rawServletPath, pathInfo.length() != 0);
        return servletPath + pathInfo;
    }

    private static String normalizeAbsolutePath(String path, boolean isRemoveTrailingSlash)
            throws IllegalStateException {
        return normalizePath(path, true, false, isRemoveTrailingSlash);
    }

    private static String normalizePath(String path, boolean isForceAbsolute, boolean isForceRelative,
            boolean isRemoveTrailingSlash) throws IllegalStateException {
        char[] pathChars = StringUtils.trim(path).toCharArray();
        int length = pathChars.length;

        // Check path and slash.
        boolean isStartsWithSlash = false;
        boolean isEndsWithSlash = false;

        if (length > 0) {
            char firstChar = pathChars[0];
            char lastChar = pathChars[length - 1];

            isStartsWithSlash = firstChar == PATH_SPLIT.charAt(0) || firstChar == '\\';
            isEndsWithSlash = lastChar == PATH_SPLIT.charAt(0) || lastChar == '\\';
        }

        StringBuilder buf = new StringBuilder(length);
        boolean isAbsolutePath = isForceAbsolute || !isForceRelative && isStartsWithSlash;
        int index = isStartsWithSlash ? 0 : NEGATIVE_ONE;
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

        if (buf.length() > 0 && (!isEndsWithSlash || isRemoveTrailingSlash)) {
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

    private static int indexOfSlash(char[] chars, int beginIndex, boolean isSlash) {
        int index = beginIndex;

        for (; index < chars.length; index++) {
            char ch = chars[index];

            if (isSlash) {
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

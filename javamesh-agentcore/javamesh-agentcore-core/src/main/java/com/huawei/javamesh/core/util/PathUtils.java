package com.huawei.javamesh.core.util;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.huawei.javamesh.core.util.StringUtils.*;
import static java.util.Arrays.asList;


/**
 * Path Utilities class
 *
 * @since 2.7.6
 */
public interface PathUtils {

    static String buildPath(String rootPath, String... subPaths) {

        Set<String> paths = new LinkedHashSet<>();
        paths.add(rootPath);
        paths.addAll(asList(subPaths));

        return normalize(paths.stream()
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(SLASH)));
    }

    /**
     * Normalize path:
     * <ol>
     * <li>To remove query string if presents</li>
     * <li>To remove duplicated slash("/") if exists</li>
     * </ol>
     *
     * @param path path to be normalized
     * @return a normalized path if required
     */
    static String normalize(String path) {
        if (isEmpty(path)) {
            return SLASH;
        }
        String normalizedPath = path;
        int index = normalizedPath.indexOf(QUESTION_MASK);
        if (index > -1) {
            normalizedPath = normalizedPath.substring(0, index);
        }

        while (normalizedPath.contains("//")) {
            normalizedPath = normalizedPath.replace("//","/");
        }

        return normalizedPath;
    }



}

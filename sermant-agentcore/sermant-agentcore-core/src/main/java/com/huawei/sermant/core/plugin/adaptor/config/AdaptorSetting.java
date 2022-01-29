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

package com.huawei.sermant.core.plugin.adaptor.config;

import com.huawei.sermant.core.plugin.adaptor.common.AdaptorConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 适配器设定
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public class AdaptorSetting {
    /**
     * 适配器名称，用于校验，必须与{@link com.huawei.sermant.core.plugin.config.PluginSetting}中的配置值一致
     */
    private String adaptorName;

    /**
     * 适配器类型
     */
    private AdaptorLoadType adaptorLoadType = AdaptorLoadType.SYSTEM;

    /**
     * 运行环境构建策略
     */
    private RewriteStrategy rewriteStrategy = RewriteStrategy.INCREMENT;

    /**
     * 适配器包，支持通配符匹配多个jar包，支持','拼接匹配多个jar包
     */
    private String adaptorJar = "*.jar";

    /**
     * 运行环境相对目录
     */
    private String executeEnvDir = AdaptorConstants.ADAPTOR_EXEC_ENV_DEFAULT_DIR;

    /**
     * 文件映射，准备运行环境时，将依据该映射拷贝运行环境源目录以外的内容
     */
    private MappingInfo[] fileMappings;

    /**
     * 包名映射，准备运行环境时，如果适配器类型为{@link AdaptorLoadType#SYSTEM}，则依据该映射修正全限定名
     */
    private List<ShadeMappingInfo> packageMappings;

    /**
     * 在准备运行环境时，如果需要修正全限定名，该集合可以按包名排除一些jar包
     */
    private Set<String> excludes;

    /**
     * 外部agent启动参数
     */
    private String agentMainArg = "";

    public String getAdaptorName() {
        return adaptorName;
    }

    public void setAdaptorName(String adaptorName) {
        this.adaptorName = adaptorName;
    }

    public AdaptorLoadType getAdaptorLoadType() {
        return adaptorLoadType;
    }

    public void setAdaptorLoadType(AdaptorLoadType adaptorLoadType) {
        this.adaptorLoadType = adaptorLoadType;
    }

    public RewriteStrategy getRewriteStrategy() {
        return rewriteStrategy;
    }

    public void setRewriteStrategy(RewriteStrategy rewriteStrategy) {
        this.rewriteStrategy = rewriteStrategy;
    }

    public String getAdaptorJar() {
        return adaptorJar;
    }

    public void setAdaptorJar(String adaptorJar) {
        this.adaptorJar = adaptorJar;
    }

    public String getExecuteEnvDir() {
        return executeEnvDir;
    }

    public void setExecuteEnvDir(String executeEnvDir) {
        this.executeEnvDir = executeEnvDir;
    }

    public MappingInfo[] getFileMappings() {
        return fileMappings;
    }

    public void setFileMappings(MappingInfo[] fileMappings) {
        this.fileMappings = fileMappings;
    }

    public List<ShadeMappingInfo> getPackageMappings() {
        return packageMappings;
    }

    public void setPackageMappings(List<ShadeMappingInfo> packageMappings) {
        this.packageMappings = packageMappings;
    }

    public Set<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }

    public String getAgentMainArg() {
        return agentMainArg;
    }

    public void setAgentMainArg(String agentMainArg) {
        this.agentMainArg = agentMainArg;
    }

    @Override
    public String toString() {
        return "AdaptorSetting{"
                + "name='" + adaptorName + '\''
                + ", adaptorLoadType=" + adaptorLoadType
                + ", rewriteStrategy=" + rewriteStrategy
                + ", adaptorJar='" + adaptorJar + '\''
                + ", executeEnvDir='" + executeEnvDir + '\''
                + ", fileMappings=" + Arrays.toString(fileMappings)
                + ", packageMappings=" + packageMappings
                + ", excludes=" + excludes
                + ", agentMainArg='" + agentMainArg + '\''
                + '}';
    }

    /**
     * 适配器加载类型，该类型决定适配器的加载方式
     */
    public enum AdaptorLoadType {
        /**
         * 系统型适配器，将使用系统类加载器加载适配包，需要修正全限定名以避免类冲突的情况
         */
        SYSTEM,

        /**
         * 独立型适配器，将单独使用一个类加载加载适配包，将优先使用适配包中的类，可有效避免类冲突问题
         * <p>注意，使用该类型可能导致适配的agent无法正确增强，需要严格审视适配的agent代码后，才能考虑使用该类型
         */
        STAND_ALONE
    }

    /**
     * 运行环境目录拷贝策略
     */
    public enum RewriteStrategy {
        /**
         * 完全重写，如果运行环境目录存在，直接删除，遇到已存在文件时，直接覆盖
         */
        REWRITE(false, true, true),

        /**
         * 文件覆盖，遇到已存在文件时，直接覆盖
         */
        COVER(false, false, true),

        /**
         * 增量补充，仅当遇到不存在的文件时，才会写出
         */
        INCREMENT(false, false, false),

        /**
         * 不重写，如果运行环境目录存在，直接跳过
         */
        NOT_REWRITE(true, false, false);

        /**
         * 是否当运行环境目录存在时直接跳过
         */
        private final boolean isSkipWhenExistEnable;

        /**
         * 是否直接重写运行环境目录
         */
        private final boolean isRewriteExecEnvDirEnable;

        /**
         * 遇到已存在的文件是否重写
         */
        private final boolean isRewriteFileEnable;

        RewriteStrategy(boolean isSkipWhenExistEnable, boolean isRewriteExecEnvDirEnable, boolean isRewriteFileEnable) {
            this.isSkipWhenExistEnable = isSkipWhenExistEnable;
            this.isRewriteExecEnvDirEnable = isRewriteExecEnvDirEnable;
            this.isRewriteFileEnable = isRewriteFileEnable;
        }

        public boolean isSkipWhenExistEnable() {
            return isSkipWhenExistEnable;
        }

        public boolean isRewriteExecEnvDirEnable() {
            return isRewriteExecEnvDirEnable;
        }

        public boolean isRewriteFileEnable() {
            return isRewriteFileEnable;
        }
    }

    /**
     * 映射信息
     */
    public static class MappingInfo {
        /**
         * 源格式：
         * <pre>
         *     1.对文件映射来说，是相对路径
         *     2.对包名映射来说，是全限定包名
         * </pre>
         */
        private String sourcePattern;

        /**
         * 目标格式：
         * <pre>
         *     1.对文件映射来说，是相对路径
         *     2.对包名映射来说，是全限定包名
         * </pre>
         */
        private String targetPattern;

        public String getSourcePattern() {
            return sourcePattern;
        }

        public void setSourcePattern(String sourcePattern) {
            this.sourcePattern = sourcePattern;
        }

        public String getTargetPattern() {
            return targetPattern;
        }

        public void setTargetPattern(String targetPattern) {
            this.targetPattern = targetPattern;
        }
    }

    /**
     * 包名映射信息，继承于{@link MappingInfo}
     */
    public static class ShadeMappingInfo extends MappingInfo {
        /**
         * 源包名的路径形式
         */
        private String sourcePathPattern;

        /**
         * 目标包名的路径形式
         */
        private String targetPathPattern;

        public String getSourcePathPattern() {
            if (sourcePathPattern == null) {
                sourcePathPattern = getSourcePattern().replace('.', '/');
            }
            return sourcePathPattern;
        }

        public String getTargetPathPattern() {
            if (targetPathPattern == null) {
                targetPathPattern = getTargetPattern().replace('.', '/');
            }
            return targetPathPattern;
        }

        /**
         * 类名满足源全限定名前缀的，修正为目标全限定名
         *
         * @param className 类名
         * @return 修正后类名
         */
        public String relocateClass(String className) {
            if (className.startsWith(getSourcePattern())) {
                return className.replaceFirst(getSourcePattern(), getTargetPattern());
            }
            return className;
        }

        /**
         * 路径名满足源路径名前缀的，修正为目标路径名
         *
         * @param path 路径名
         * @return 修正后的路径名
         */
        public String relocatePath(String path) {
            if (path.startsWith(getSourcePathPattern())) {
                return path.replaceFirst(getSourcePathPattern(), getTargetPathPattern());
            }
            return path;
        }
    }
}

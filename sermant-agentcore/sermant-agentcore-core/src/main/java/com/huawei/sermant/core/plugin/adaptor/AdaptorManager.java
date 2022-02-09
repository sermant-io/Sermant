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

package com.huawei.sermant.core.plugin.adaptor;

import com.huawei.sermant.core.common.BootArgsIndexer;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.config.ConfigManager;
import com.huawei.sermant.core.exception.SchemaException;
import com.huawei.sermant.core.plugin.adaptor.collector.AdaptorCollectorManager;
import com.huawei.sermant.core.plugin.adaptor.common.AdaptorConstants;
import com.huawei.sermant.core.plugin.adaptor.config.AdaptorConfig;
import com.huawei.sermant.core.plugin.adaptor.config.AdaptorSetting;
import com.huawei.sermant.core.plugin.adaptor.remapper.AdaptorShader;
import com.huawei.sermant.core.plugin.adaptor.service.AdaptorServiceManager;
import com.huawei.sermant.core.plugin.classloader.PluginClassLoader;
import com.huawei.sermant.core.plugin.common.PluginConstant;
import com.huawei.sermant.core.plugin.common.PluginSchemaValidator;
import com.huawei.sermant.core.utils.FileUtils;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * 适配器管理器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
public class AdaptorManager {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private AdaptorManager() {
    }

    /**
     * 初始化所有适配器
     *
     * @param adaptorNames    适配器名称集
     * @param instrumentation Instrumentation对象
     * @return 是否初始化成功
     */
    public static boolean initAdaptors(List<String> adaptorNames, Instrumentation instrumentation) {
        final AdaptorConfig config = ConfigManager.getConfig(AdaptorConfig.class);
        if (!config.isLoadExtAgentEnable()) {
            return false;
        }
        if (adaptorNames == null || adaptorNames.isEmpty()) {
            return false;
        }
        final String pluginPackage;
        try {
            pluginPackage = BootArgsIndexer.getPluginPackageDir().getCanonicalPath();
        } catch (IOException ignored) {
            LOGGER.warning("Resolve plugin package failed. ");
            return false;
        }
        for (String adaptorName : adaptorNames) {
            initAdaptor(adaptorName, pluginPackage, config, instrumentation);
        }
        addStopHook();
        return true;
    }

    /**
     * 添加关闭钩子
     */
    private static void addStopHook() {
        AdaptorServiceManager.addStopHook();
    }

    /**
     * 初始化一个适配器，检查必要参数，并调用{@link #doInitAdaptor}
     *
     * @param adaptorName     适配器名称
     * @param pluginPackage   插件包目录
     * @param config          适配器配置
     * @param instrumentation Instrumentation对象
     */
    private static void initAdaptor(String adaptorName, String pluginPackage, AdaptorConfig config,
            Instrumentation instrumentation) {
        final String adaptorPath = pluginPackage + File.separatorChar + adaptorName;
        if (!new File(adaptorPath).exists()) {
            LOGGER.warning(String.format(Locale.ROOT,
                    "Adaptor directory %s does not exist, so skip initializing %s. ", adaptorPath, adaptorName));
            return;
        }
        final AdaptorSetting setting = loadAdaptorSetting(adaptorPath);
        if (setting == null) {
            LOGGER.warning(String.format(Locale.ROOT,
                    "Cannot load adaptor setting file in %s, so skip initializing %s. ", adaptorPath, adaptorName));
            return;
        }
        if (!setting.getAdaptorName().equals(adaptorName)) {
            LOGGER.warning(String.format(Locale.ROOT, "Adaptor name is not correct, giving %s, but expecting %s. ",
                    setting.getAdaptorName(), adaptorName));
            return;
        }
        final File[] adaptors = FileUtils.getChildrenByWildcard(new File(adaptorPath), setting.getAdaptorJar());
        if (adaptors.length <= 0) {
            LOGGER.warning(String.format(Locale.ROOT, "Missing matched adaptor jars in %s, so skip initializing %s. ",
                    adaptorPath, adaptorName));
            return;
        }
        try {
            doInitAdaptor(adaptorPath, adaptors, config, setting, instrumentation);
        } catch (IOException ignored) {
            LOGGER.warning(String.format(Locale.ROOT, "Initialize adaptor %s failed. ", adaptorName));
        }
    }

    /**
     * 初始化适配器，主要包含以下流程：
     * <pre>
     *     1.加载适配包
     *     2.准备适配器运行环境
     *     3.加载相关资源：适配器服务和适配器插件收集器
     *     4.设置默认适配器版本
     * </pre>
     *
     * @param adaptorPath     当前适配器的路径
     * @param adaptors        当前适配器的适配包集
     * @param config          适配器配置
     * @param setting         当前适配器设定
     * @param instrumentation Instrumentation对象
     * @throws IOException 初始化适配器失败
     */
    private static void doInitAdaptor(String adaptorPath, File[] adaptors, AdaptorConfig config, AdaptorSetting setting,
            Instrumentation instrumentation) throws IOException {
        final ClassLoader loader = loadAdaptors(setting, adaptors, instrumentation);
        final String execEnvDir = getExecEnvDir(config, setting, adaptorPath);
        prepareExecEnv(execEnvDir, setting, adaptorPath);
        loadResources(setting.getAgentMainArg(), new File(execEnvDir), loader, instrumentation);
        setDefaultVersion(setting.getAdaptorName());
    }

    /**
     * 设置默认的适配器版本
     *
     * @param pluginName 适配器名称
     */
    private static void setDefaultVersion(String pluginName) {
        PluginSchemaValidator.setDefaultVersion(pluginName);
    }

    /**
     * 加载相关资源：适配器服务和适配器插件收集器
     *
     * @param agentMainArg    外部agent启动参数
     * @param execEnvDir      适配器运行目录
     * @param loader          适配包加载器
     * @param instrumentation Instrumentation对象
     */
    private static void loadResources(String agentMainArg, File execEnvDir, ClassLoader loader,
            Instrumentation instrumentation) {
        AdaptorServiceManager.loadServices(agentMainArg, execEnvDir, loader, instrumentation);
        AdaptorCollectorManager.loadCollectors(loader);
    }

    /**
     * 加载适配器设定文件
     *
     * @param adaptorPath 适配器目录
     * @return 适配器设定
     */
    private static AdaptorSetting loadAdaptorSetting(String adaptorPath) {
        final File adaptorSettingFile = PluginConstant.getPluginConfigFile(adaptorPath);
        if (!adaptorSettingFile.exists() || !adaptorSettingFile.isFile()) {
            return null;
        }
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(adaptorSettingFile);
            return new Yaml().loadAs(inputStream, AdaptorSetting.class);
        } catch (FileNotFoundException ignored) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                    LOGGER.warning("Unexpected exception occurs. ");
                }
            }
        }
    }

    /**
     * 加载适配包：
     * <pre>
     *     1.如果适配器加载类型为{@link AdaptorSetting.AdaptorLoadType#SYSTEM}，使用系统类加载器加载
     *     2.如果适配器加载类型为{@link AdaptorSetting.AdaptorLoadType#STAND_ALONE}，则单独创建{@link PluginClassLoader}加载
     * </pre>
     *
     * @param setting         适配器设定
     * @param adaptors        适配包集
     * @param instrumentation Instrumentation对象
     * @return 加载适配包的类加载器
     * @throws IOException 无法加载适配包
     */
    private static ClassLoader loadAdaptors(AdaptorSetting setting, File[] adaptors, Instrumentation instrumentation)
            throws IOException {
        final String adaptorName = setting.getAdaptorName();
        switch (setting.getAdaptorLoadType()) {
            case SYSTEM:
                checkSchema(adaptorName, adaptors, new JarFileHandler() {
                    @Override
                    public void handle(JarFile jarFile) {
                        instrumentation.appendToSystemClassLoaderSearch(jarFile);
                    }
                });
                return ClassLoader.getSystemClassLoader();
            case STAND_ALONE:
                checkSchema(adaptorName, adaptors, null);
                return new PluginClassLoader(toUrls(adaptors));
            default:
                throw new UnsupportedOperationException(String.format(Locale.ROOT,
                        "Unknown adaptor type %s in setting [%s]. ", setting.getAdaptorLoadType(), setting));
        }
    }

    /**
     * 检查适配包元数据，元数据校验异常时，抛出{@link SchemaException}
     *
     * @param adaptorName 适配器名称
     * @param adaptors    适配包集
     * @param handler     适配包加载为JarFile后的处理器
     * @throws IOException 无法加载适配包
     */
    private static void checkSchema(String adaptorName, File[] adaptors, JarFileHandler handler) throws IOException {
        for (File file : adaptors) {
            final JarFile jarFile = new JarFile(file);
            if (!PluginSchemaValidator.checkSchema(adaptorName, jarFile)) {
                throw new SchemaException(SchemaException.UNEXPECTED_EXT_JAR, file.getPath());
            }
            if (handler != null) {
                handler.handle(jarFile);
            }
        }
    }

    /**
     * 适配包集转换为URL集
     *
     * @param adaptors 适配包集
     * @return URL集
     * @throws MalformedURLException 转换失败
     */
    private static URL[] toUrls(File[] adaptors) throws MalformedURLException {
        final List<URL> urls = new ArrayList<URL>();
        for (File adaptor : adaptors) {
            urls.add(adaptor.toURI().toURL());
        }
        return urls.toArray(new URL[0]);
    }

    /**
     * 获取运行环境目录：
     * <pre>
     *     1.优先取{@link AdaptorConfig#getExecuteEnvDir}/{@link AdaptorSetting#getAdaptorName}
     *     2.次优先取{@code adaptorPath}/{@link AdaptorSetting#getExecuteEnvDir}
     *     3.最低优先级取{@code adaptorPath}/temp
     * </pre>
     *
     * @param config      适配器配置
     * @param setting     适配器设定
     * @param adaptorPath 适配器路径
     * @return 运行环境目录
     */
    private static String getExecEnvDir(AdaptorConfig config, AdaptorSetting setting, String adaptorPath) {
        final String globalExecEnvDir = config.getExecuteEnvDir();
        if (globalExecEnvDir == null || globalExecEnvDir.isEmpty()) {
            final String adaptorExecEnvDir = setting.getExecuteEnvDir();
            if (adaptorExecEnvDir == null || adaptorExecEnvDir.isEmpty()) {
                return adaptorPath + File.separatorChar + AdaptorConstants.ADAPTOR_EXEC_ENV_DEFAULT_DIR;
            } else {
                return adaptorPath + File.separatorChar + adaptorExecEnvDir;
            }
        } else {
            return globalExecEnvDir + File.separatorChar + setting.getAdaptorName();
        }
    }

    /**
     * 准备运行环境：
     * <pre>
     *     1.如果适配器加载类型为{@link AdaptorSetting.AdaptorLoadType#SYSTEM}，则需要该规则修改全限定名
     *     2.如果适配器加载类型为{@link AdaptorSetting.AdaptorLoadType#STAND_ALONE}，直接将备份运行环境文件夹拷贝即可
     * </pre>
     *
     * @param execEnvDir  运行环境目录
     * @param setting     适配器设定
     * @param adaptorPath 适配器文件夹路径
     * @throws IOException 准备运行环境失败
     */
    private static void prepareExecEnv(String execEnvDir, AdaptorSetting setting, String adaptorPath)
            throws IOException {
        final String srcEnvDir = adaptorPath + File.separatorChar + AdaptorConstants.ADAPTOR_ENV_SOURCE_DIR;
        final AdaptorSetting.RewriteStrategy strategy = setting.getRewriteStrategy();
        switch (setting.getAdaptorLoadType()) {
            case SYSTEM:
                final List<AdaptorSetting.ShadeMappingInfo> packageMappings = setting.getPackageMappings();
                final Set<String> excludes = setting.getExcludes();
                AdaptorShader.shade(srcEnvDir, execEnvDir, strategy, packageMappings, excludes);
                for (AdaptorSetting.MappingInfo mapping : setting.getFileMappings()) {
                    final String sourcePath = adaptorPath + File.separatorChar + mapping.getSourcePattern();
                    final String targetPath = execEnvDir + File.separatorChar + mapping.getTargetPattern();
                    AdaptorShader.shade(sourcePath, targetPath, strategy, packageMappings, excludes);
                }
                break;
            case STAND_ALONE:
                copyAllFiles(srcEnvDir, execEnvDir, strategy);
                for (AdaptorSetting.MappingInfo mapping : setting.getFileMappings()) {
                    final String sourcePath = adaptorPath + File.separatorChar + mapping.getSourcePattern();
                    final String targetPath = execEnvDir + File.separatorChar + mapping.getTargetPattern();
                    copyAllFiles(sourcePath, targetPath, strategy);
                }
                break;
            default:
                throw new UnsupportedOperationException(String.format(Locale.ROOT,
                        "Unknown adaptor type %s in setting [%s]. ", setting.getAdaptorLoadType(), setting));
        }
    }

    /**
     * 拷贝全部文件
     *
     * @param sourcePath 拷贝源文件夹
     * @param targetPath 拷贝目标文件夹
     * @param strategy   拷贝策略
     * @throws IOException 拷贝文件夹失败
     */
    private static void copyAllFiles(String sourcePath, String targetPath, AdaptorSetting.RewriteStrategy strategy)
            throws IOException {
        final File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            return;
        }
        final File targetFile = new File(targetPath);
        if (targetFile.exists() && strategy.isSkipWhenExistEnable()) {
            return;
        }
        if (strategy.isRewriteExecEnvDirEnable()) {
            FileUtils.deleteDirs(targetFile);
        }
        FileUtils.copyAllFiles(sourceFile, targetPath, strategy.isRewriteFileEnable());
    }

    /**
     * {@link JarFile}处理器，用于校验jar包元信息时植入额外操作
     */
    private interface JarFileHandler {
        /**
         * 处理jar包
         *
         * @param jarFile jar包
         */
        void handle(JarFile jarFile);
    }
}

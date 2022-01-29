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

package com.huawei.sermant.core.plugin.adaptor.remapper;

import com.huawei.sermant.core.common.CommonConstant;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.plugin.adaptor.config.AdaptorSetting;
import com.huawei.sermant.core.utils.FileUtils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;

/**
 * 修正jar包中所有类全限定名的shader
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-18
 */
public class AdaptorShader {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 缓冲区大小
     */
    private static final int BUFFER_SIZE = 1024 * 16;

    private AdaptorShader() {
    }

    /**
     * 修正目录下所有jar包中所有类全限定名
     *
     * @param sourcePath    源目录
     * @param targetPath    输出目标目录
     * @param strategy      文件重写策略
     * @param shadeMappings 修正mapping
     * @param excludes      无需修正的jar包
     */
    public static void shade(String sourcePath, String targetPath, AdaptorSetting.RewriteStrategy strategy,
            List<AdaptorSetting.ShadeMappingInfo> shadeMappings, Set<String> excludes) {
        final File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            return;
        }
        final File targetFile = new File(targetPath);
        if (strategy.isSkipWhenExistEnable() && targetFile.exists()) {
            return;
        }
        if (strategy.isRewriteExecEnvDirEnable()) {
            FileUtils.deleteDirs(targetFile);
        }
        final CopyConsumer copyConsumer = CopyConsumer.build(sourcePath, targetPath, strategy.isRewriteFileEnable());
        final ShadeConsumer shadeConsumer =
                ShadeConsumer.build(sourcePath, targetPath, strategy.isRewriteFileEnable(), shadeMappings,
                        copyConsumer);
        if (copyConsumer == null || shadeConsumer == null) {
            return;
        }
        foreachFile(sourceFile, excludes, shadeConsumer, copyConsumer);
    }

    /**
     * 对所有文件进行操作
     *
     * @param file            文件
     * @param excludes        无需修正的jar包
     * @param jarConsumer     对jar包进行操作
     * @param defaultConsumer 对其他文件或无需修正的jar包进行的操作
     */
    private static void foreachFile(File file, Set<String> excludes, FileConsumer jarConsumer,
            FileConsumer defaultConsumer) {
        if (file.isFile()) {
            final String fileName = file.getName();
            if (fileName.endsWith(".jar") && (excludes == null || !excludes.contains(fileName))) {
                jarConsumer.consume(file);
            } else {
                defaultConsumer.consume(file);
            }
        } else {
            final File[] subFiles = file.listFiles();
            if (subFiles == null) {
                return;
            }
            for (File subFile : subFiles) {
                foreachFile(subFile, excludes, jarConsumer, defaultConsumer);
            }
        }
    }

    /**
     * 文件消费者，为兼容1.6
     */
    private interface FileConsumer {
        void consume(File file);
    }

    /**
     * shade操作消费者
     */
    private static class ShadeConsumer implements FileConsumer {
        private static final String SERVICE_RESOURCE_DIR = "META-INF/services/";

        /**
         * 源路径
         */
        private final String sourcePath;
        /**
         * 目标路径
         */
        private final String targetPath;
        private final boolean isRewriteFileEnable;
        /**
         * 自定义Remapper，用于修正全限定名和路径
         */
        private final AdaptorRemapper adaptorRemapper;
        /**
         * 默认消费者，修正失败时将执行默认操作
         */
        private final FileConsumer defaultConsumer;

        private ShadeConsumer(String sourcePath, String targetPath, boolean isRewriteFileEnable,
                AdaptorRemapper adaptorRemapper,
                FileConsumer defaultConsumer) {
            this.sourcePath = sourcePath;
            this.targetPath = targetPath;
            this.isRewriteFileEnable = isRewriteFileEnable;
            this.adaptorRemapper = adaptorRemapper;
            this.defaultConsumer = defaultConsumer;
        }

        /**
         * 校验路径并构建ShadeConsumer
         *
         * @param sourcePath      源jar包路径
         * @param targetPath      目标jar包路径
         * @param shadeMappings   修正mapping
         * @param defaultConsumer 默认的文件处理器
         * @return ShadeConsumer对象
         */
        static ShadeConsumer build(String sourcePath, String targetPath, boolean isRewriteFileEnable,
                List<AdaptorSetting.ShadeMappingInfo> shadeMappings,
                FileConsumer defaultConsumer) {
            try {
                return new ShadeConsumer(new File(sourcePath).getCanonicalPath(),
                        new File(targetPath).getCanonicalPath(), isRewriteFileEnable,
                        new AdaptorRemapper(shadeMappings), defaultConsumer);
            } catch (IOException ignored) {
                return null;
            }
        }

        @Override
        public void consume(File file) {
            try {
                final File targetFile = new File(file.getCanonicalPath().replace(sourcePath, targetPath));
                if (!isRewriteFileEnable && targetFile.exists()) {
                    return;
                }
                if (!FileUtils.createParentDir(targetFile)) {
                    return;
                }
                JarOutputStream outputStream = null;
                try {
                    outputStream = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(targetFile)));
                    shadeJar(file, outputStream);
                } finally {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            } catch (IOException ignored) {
                defaultConsumer.consume(file);
            }
        }

        /**
         * 修正jar包
         *
         * @param file         jar包
         * @param outputStream 目标输出流
         * @throws IOException 修正jar包失败
         */
        private void shadeJar(File file, JarOutputStream outputStream) throws IOException {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(file);
                final Set<String> duplicateSet = new HashSet<String>();
                for (Enumeration<JarEntry> enumeration = jarFile.entries(); enumeration.hasMoreElements(); ) {
                    JarEntry entry = enumeration.nextElement();
                    if (entry.isDirectory()) {
                        continue;
                    }
                    InputStream inputStream = null;
                    try {
                        inputStream = jarFile.getInputStream(entry);
                        shadeEntry(entry, inputStream, outputStream, duplicateSet);
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                }
            } finally {
                if (jarFile != null) {
                    jarFile.close();
                }
            }
        }

        /**
         * 修正jar包中文件
         *
         * @param entry        jar包中文件
         * @param inputStream  该文件的输入流
         * @param outputStream 目标jar包输出流
         * @param duplicateSet 目录去重集
         * @throws IOException 修正文件失败
         */
        private void shadeEntry(JarEntry entry, InputStream inputStream, JarOutputStream outputStream,
                Set<String> duplicateSet) throws IOException {
            final String entryPath = entry.getName();
            final long entryTime = entry.getTime();
            final String remappedPath = adaptorRemapper.map(entryPath);
            createParentDir(remappedPath, entryTime, outputStream, duplicateSet);
            if (remappedPath.endsWith(".class")) {
                remapClass(entryPath, remappedPath, entryTime, inputStream, outputStream);
            } else if (remappedPath.startsWith(SERVICE_RESOURCE_DIR)) {
                remapServiceResource(remappedPath, entryTime, inputStream, outputStream);
            } else {
                copyEntry(remappedPath, entryTime, inputStream, outputStream);
            }
        }

        /**
         * 拷贝jar包中普通文件
         *
         * @param entryPath    文件路径
         * @param entryTime    文件创建时间
         * @param inputStream  文件输入流
         * @param outputStream 目标jar包输出流
         * @throws IOException 拷贝文件失败
         */
        private void copyEntry(String entryPath, long entryTime, InputStream inputStream, JarOutputStream outputStream)
                throws IOException {
            final JarEntry newEntry = new JarEntry(entryPath);
            newEntry.setTime(entryTime);
            outputStream.putNextEntry(newEntry);
            final byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = inputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, length);
            }
        }

        /**
         * 对class文件进行重新修正
         *
         * @param entryPath   class文件路径
         * @param inputStream 文件的输入流
         * @throws IOException 修正class文件失败
         */
        private void remapClass(String entryPath, String remappedPath, long entryTime, InputStream inputStream,
                JarOutputStream outputStream) throws IOException {
            final ClassReader classReader = new ClassReader(inputStream);
            final ClassWriter classWriter = new ClassWriter(0);
            final String packagePath = entryPath.substring(0, entryPath.lastIndexOf('/') + 1);
            final ClassVisitor classVisitor = new ClassRemapper(classWriter, adaptorRemapper) {
                @Override
                public void visitSource(final String source, final String debug) {
                    if (source == null) {
                        super.visitSource(null, debug);
                    } else {
                        final String remappedSource = remapper.map(packagePath + source);
                        super.visitSource(remappedSource.substring(remappedSource.lastIndexOf('/') + 1), debug);
                    }
                }
            };
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
            final JarEntry entry = new JarEntry(remappedPath);
            entry.setTime(entryTime);
            outputStream.putNextEntry(entry);
            outputStream.write(classWriter.toByteArray());
        }

        /**
         * 对spi配置文件进行重新修正
         *
         * @param remappedPath spi配置文件路径
         * @param entryTime    文件创建时间
         * @param inputStream  文件的输入流
         * @param outputStream 文件的输出流
         * @throws IOException 修正spi配置文件失败
         */
        private void remapServiceResource(String remappedPath, long entryTime,
                InputStream inputStream, JarOutputStream outputStream) throws IOException {
            final String serviceResourceName = remappedPath.substring(SERVICE_RESOURCE_DIR.length());
            final String remappedName = adaptorRemapper.mapServiceResource(serviceResourceName);
            final JarEntry entry = new JarEntry(SERVICE_RESOURCE_DIR + remappedName);
            entry.setTime(entryTime);
            outputStream.putNextEntry(entry);
            final String separator = System.getProperty("line.separator");
            final BufferedReader br = new BufferedReader(
                    new InputStreamReader(inputStream, CommonConstant.DEFAULT_CHARSET));
            String line = br.readLine();
            while (line != null) {
                outputStream.write((adaptorRemapper.mapServiceResource(line) + separator)
                        .getBytes(CommonConstant.DEFAULT_CHARSET));
                line = br.readLine();
            }
        }

        /**
         * 验证并创建父目录
         *
         * @param filePath     文件路径
         * @param entryTime    文件创建时间
         * @param outputStream 目标jar包输出流
         * @param duplicateSet 目录去重集
         * @throws IOException 创建目录失败
         */
        private void createParentDir(String filePath, long entryTime, JarOutputStream outputStream,
                Set<String> duplicateSet) throws IOException {
            final int index = filePath.lastIndexOf('/');
            if (index > 0) {
                final String dir = filePath.substring(0, index);
                if (!duplicateSet.contains(dir)) {
                    mkdirs(dir, entryTime, outputStream, duplicateSet);
                }
            }
        }

        /**
         * 在jar包中创建完整的一个包路径
         *
         * @param packagePath  包路径
         * @param entryTime    创建时间
         * @param outputStream 目标jar包输出流
         * @param duplicateSet 目录去重集
         * @throws IOException 创建目录失败
         */
        private void mkdirs(String packagePath, long entryTime, JarOutputStream outputStream, Set<String> duplicateSet)
                throws IOException {
            if (packagePath.lastIndexOf('/') > 0) {
                final String parent = packagePath.substring(0, packagePath.lastIndexOf('/'));
                if (!duplicateSet.contains(parent)) {
                    mkdirs(parent, entryTime, outputStream, duplicateSet);
                }
            }
            final JarEntry entry = new JarEntry(packagePath + '/');
            entry.setTime(entryTime);
            outputStream.putNextEntry(entry);
            duplicateSet.add(packagePath);
        }
    }

    /**
     * 用于复制文件的处理器
     */
    private static class CopyConsumer implements FileConsumer {
        /**
         * 源文件路径
         */
        private final String sourcePath;
        /**
         * 目标文件路径
         */
        private final String targetPath;

        /**
         * 是否允许覆盖已存在的文件
         */
        private final boolean isRewriteFileEnable;

        private CopyConsumer(String sourcePath, String targetPath, boolean isRewriteFileEnable) {
            this.sourcePath = sourcePath;
            this.targetPath = targetPath;
            this.isRewriteFileEnable = isRewriteFileEnable;
        }

        /**
         * 构建CopyConsumer
         *
         * @param sourcePath 源文件路径
         * @param targetPath 目标文件路径
         * @return CopyConsumer对象
         */
        static CopyConsumer build(String sourcePath, String targetPath, boolean isRewriteFileEnable) {
            try {
                return new CopyConsumer(new File(sourcePath).getCanonicalPath(),
                        new File(targetPath).getCanonicalPath(), isRewriteFileEnable);
            } catch (IOException ignored) {
                return null;
            }
        }

        @Override
        public void consume(File file) {
            try {
                final File targetFile = new File(file.getCanonicalPath().replace(sourcePath, targetPath));
                if (!isRewriteFileEnable && targetFile.exists()) {
                    return;
                }
                if (FileUtils.createParentDir(targetFile)) {
                    FileUtils.copyFile(file, targetFile);
                }
            } catch (IOException ignored) {
                LOGGER.warning("Unexpected exception occurs. ");
            }
        }
    }
}

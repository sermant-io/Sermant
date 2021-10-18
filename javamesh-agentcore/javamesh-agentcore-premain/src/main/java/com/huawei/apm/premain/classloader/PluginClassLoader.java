package com.huawei.apm.premain.classloader;

import com.huawei.apm.premain.agent.ByteBuddyAgentBuilder;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import com.huawei.apm.premain.lubanops.utils.LibPathUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * 插件加载器定义
 */
public class PluginClassLoader extends URLClassLoader {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final PluginClassLoader INSTANCE =
            AccessController.doPrivileged(new PluginClassLoaderPrivilegedAction());

    private List<JavaFileWrapper> javaFileWrappers;

    private PluginClassLoader(URL[] urls, ClassLoader classLoader) {
        super(urls, classLoader);
    }

    public PluginClassLoader(ClassLoader parent) {
        this(getPluginUrl().toArray(new URL[0]), parent);
    }

    public static PluginClassLoader getDefault() {
        return INSTANCE;
    }

    private static List<URL> getPluginUrl() {
        List<URL> jarUrlList = new ArrayList<URL>();
        try {
            for (File file : getPluginJarFiles()) {
                jarUrlList.add(file.toURI().toURL());
            }
        } catch (Exception ex) {
            LogFactory.getLogger().warning(String.format("add jar url failed !{%s}", ex.getMessage()));
        }
        return jarUrlList;
    }

    private static List<File> getPluginJarFiles() {
        final File pluginBaseDir = new File(LibPathUtils.getPluginsPath());
        if (!pluginBaseDir.exists() || !pluginBaseDir.isDirectory()) {
            return Collections.emptyList();
        }
        final File[] pluginDirs = pluginBaseDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.equals(LibPathUtils.getLubanOpsDirName());
            }
        });
        if (pluginDirs == null) {
            return Collections.emptyList();
        }
        final List<File> pluginJarList = new ArrayList<File>();
        for (File pluginDir : pluginDirs) {
            final File[] pluginJars = pluginDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }
            });
            if (pluginJars == null) {
                continue;
            }
            for (File pluginJar : pluginJars) {
                if (pluginJar.isFile()) {
                    pluginJarList.add(pluginJar);
                }
            }
        }
        return pluginJarList;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        final List<JavaFileWrapper> javaFiles = getJavaFiles();
        String path = name.replace('.', '/').concat(".class");
        for (JavaFileWrapper javaFileWrapper : javaFiles) {
            try {
                if (javaFileWrapper.jarFile.getEntry(path) == null) {
                    continue;
                }
                URL classFileUrl = new URL("jar:file:" + javaFileWrapper.path + "!/" + path);
                byte[] data;
                BufferedInputStream is = null;
                ByteArrayOutputStream byteOutStream = null;
                try {
                    is = new BufferedInputStream(classFileUrl.openStream());
                    byteOutStream = new ByteArrayOutputStream();
                    int ch;
                    while ((ch = is.read()) != -1) {
                        byteOutStream.write(ch);
                    }
                    data = byteOutStream.toByteArray();
                } finally {
                    if (is != null) {
                        is.close();
                    }
                    if (byteOutStream != null) {
                        byteOutStream.close();
                    }
                }
                return defineClass(name, data, 0, data.length);
            } catch (IOException e) {
                LOGGER.warning(String.format("find {%s} class failed! {%s}", name, e.getMessage()));
            }
        }
        return super.findClass(name);
    }

    /**
     * 转换插件URL为jar包
     *
     * @return jar包列表
     */
    private List<JavaFileWrapper> getJavaFiles() {
        if (javaFileWrappers == null) {
            synchronized (PluginClassLoader.class) {
                final List<URL> pluginUrls = getPluginUrl();
                final List<JavaFileWrapper> javaFiles = new ArrayList<JavaFileWrapper>();
                for (URL url : pluginUrls) {
                    final String path = url.getPath();
                    try {
                        javaFiles.add(new JavaFileWrapper(path, new JarFile(url.getFile())));
                    } catch (IOException e) {
                        LOGGER.warning(String.format("converted to [{%s}] jar file failed!", path.substring(path.lastIndexOf('/'))));
                    }
                }
                javaFileWrappers = javaFiles;
            }
        }
        return javaFileWrappers;
    }

    private static class JavaFileWrapper {
        /**
         * jar包绝对路径
         */
        private final String path;

        /**
         * jar实体
         */
        private final JarFile jarFile;

        public JavaFileWrapper(String path, JarFile jarFile) {
            this.path = path;
            this.jarFile = jarFile;
        }
    }

    private static class PluginClassLoaderPrivilegedAction implements PrivilegedAction<PluginClassLoader> {
        @Override
        public PluginClassLoader run() {
            return new PluginClassLoader(getPluginUrl().toArray(new URL[0]),
                    ByteBuddyAgentBuilder.class.getClassLoader());
        }
    }
}

package com.huawei.apm.classloader;

import com.huawei.apm.premain.ByteBuddyAgentBuilder;
import com.lubanops.apm.bootstrap.log.LogFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * 插件加载器定义
 */
public class PluginClassLoader extends URLClassLoader {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final PluginClassLoader INSTANCE =
            AccessController.doPrivileged(new PluginClassLoaderPrivilegedAction());

    private final AtomicReference<List<JavaFileWrapper>> javaFilesReference = new AtomicReference<List<JavaFileWrapper>>(null);

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
        String path = ByteBuddyAgentBuilder.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        List<URL> jarUrlList = new ArrayList<URL>();
        try {
            File libDir = new File(path.substring(0, path.lastIndexOf("apm-javaagent.jar")) + File.separator + "plugins");
            File[] libFiles = libDir.listFiles();
            if (libFiles != null) {
                for (File file : libFiles) {
                    jarUrlList.add(file.toURI().toURL());
                }
            }
        } catch (Exception ex) {
            LogFactory.getLogger().warning(String.format("add jar url failed !{%s}", ex.getMessage()));
        }
        return jarUrlList;
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
        final List<JavaFileWrapper> javaFileWrappers = javaFilesReference.get();
        if (javaFileWrappers == null) {
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
            javaFilesReference.compareAndSet(null, javaFiles);
            return javaFiles;
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

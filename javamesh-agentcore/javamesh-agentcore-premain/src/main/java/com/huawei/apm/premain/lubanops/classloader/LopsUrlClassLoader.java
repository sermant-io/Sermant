package com.huawei.apm.premain.lubanops.classloader;

import java.net.URL;
import java.net.URLClassLoader;

public class LopsUrlClassLoader extends URLClassLoader {

    public LopsUrlClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz = findLoadedClass(name);

        try {
            if (clazz == null) {
                if (onLoadClass(name)) {
                    clazz = findClass(name);
                } else {
                    try {
                        clazz = super.loadClass(name, resolve);
                    } catch (ClassNotFoundException ignore) {
                    }
                    if (clazz == null) {
                        clazz = findClass(name);
                    }
                }
            }
            if (resolve) {
                resolveClass(clazz);
            }
        } catch (ClassNotFoundException e) {
        }
        return clazz;
    }

    private boolean onLoadClass(String name) {
        return ExcludeClass.onLoadClass(name);
    }

}

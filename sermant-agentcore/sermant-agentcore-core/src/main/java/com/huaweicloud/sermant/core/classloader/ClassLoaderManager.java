/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.classloader;

import com.huaweicloud.sermant.core.common.CommonConstant;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manage custom classloaders.
 *
 * @author luanwenfei
 * @since 2022-06-20
 */
public class ClassLoaderManager {
    private static FrameworkClassLoader frameworkClassLoader;

    private ClassLoaderManager() {
    }

    /**
     * Init custom classloaders.
     *
     * @param argsMap arguments map
     * @throws FileNotFoundException FileNotFoundException
     * @throws MalformedURLException MalformedURLException
     */
    public static void init(Map<String, Object> argsMap) throws FileNotFoundException, MalformedURLException {
        URL[] coreImplementUrls = listCoreImplementUrls(argsMap.get(CommonConstant.CORE_IMPLEMENT_DIR_KEY).toString());
        initFrameworkClassLoader(coreImplementUrls);
    }

    /**
     * For getting FrameworkClassLoader
     *
     * @return A frameworkClassLoader that has been initialized.
     */
    public static FrameworkClassLoader getFrameworkClassLoader() {
        return frameworkClassLoader;
    }

    private static void initFrameworkClassLoader(URL[] urls) {
        frameworkClassLoader = new FrameworkClassLoader(urls);
    }

    private static URL[] listCoreImplementUrls(String coreImplementPath)
        throws FileNotFoundException, MalformedURLException {
        File coreImplementDir = new File(coreImplementPath);
        if (!coreImplementDir.exists() || !coreImplementDir.isDirectory()) {
            throw new FileNotFoundException(coreImplementPath + " not found.");
        }
        File[] jars = coreImplementDir.listFiles((file, name) -> name.endsWith(".jar"));
        if (jars == null || jars.length <= 0) {
            throw new FileNotFoundException("Can not find core implement jar.");
        }
        List<URL> urlList = new ArrayList<>();
        for (File jar : jars) {
            urlList.add(jar.toURI().toURL());
        }
        return urlList.toArray(new URL[0]);
    }
}

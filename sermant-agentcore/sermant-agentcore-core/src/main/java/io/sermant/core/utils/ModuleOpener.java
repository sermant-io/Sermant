/*
 * Copyright (C) 2025-2025 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.core.utils;

import io.sermant.core.common.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Module Opener - Used to open necessary module access permissions in Java 9+ environments
 *
 * <p>Background:
 * Spring Boot 3 is based on Java 17+, where the module system has strict restrictions on reflective access.
 * Sermant plugins need to access JDK internal classes via reflection (e.g., java.net.URLConnection),
 * which throws InaccessibleObjectException in Java 9+.
 *
 * <p>Solution:
 * Leverage the Java Agent's Instrumentation API to uniformly open necessary module access at startup.
 * This is more elegant than handling exceptions at each reflection call site, and more user-friendly
 * than requiring users to configure JVM parameters.
 *
 * <p>Design Principles:
 * 1. Least Privilege: Only open modules and packages that are actually needed
 * 2. Security First: Do not open security-sensitive internal APIs
 * 3. Backward Compatibility: Automatically skip in Java 8 environments without affecting functionality
 * 4. Centralized Management: All modules that need to be opened are configured here
 *
 * @author Sermant Team
 * @since 2025-01-09
 */
public class ModuleOpener {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Configuration of modules that need to be opened
     * Key: Module name (e.g., "java.base")
     * Value: List of packages to be opened (e.g., ["java.net", "sun.net.www.protocol.http"])
     */
    private static final Map<String, Set<String>> MODULE_OPENS_CONFIG = initModuleOpensConfig();

    /**
     * Cache of Module class (only exists in Java 9+)
     */
    private static Class<?> moduleClass;

    /**
     * Whether the module system is supported (Java 9+)
     */
    private static boolean modulesSupported = false;

    static {
        try {
            // Detect if module system is supported (Java 9+)
            moduleClass = Class.forName("java.lang.Module");
            modulesSupported = true;
            LOGGER.info("Detected Java 9+ module system, module opener is enabled.");
        } catch (ClassNotFoundException e) {
            // Java 8 environment, module system not supported
            LOGGER.info("Running on Java 8, module opener is disabled.");
        }
    }

    private ModuleOpener() {
    }

    /**
     * Initialize configuration of modules that need to be opened
     *
     * <p>Configuration details:
     * <ul>
     *   <li>java.net - Reflective access to HttpURLConnection (required by springboot-registry plugin)</li>
     *   <li>sun.net.www.protocol.http - HttpClient internal implementation (required by springboot-registry plugin)</li>
     * </ul>
     *
     * @return Module opening configuration
     */
    private static Map<String, Set<String>> initModuleOpensConfig() {
        Map<String, Set<String>> config = new HashMap<>(4);

        // java.base module - JDK core classes
        Set<String> javaBasePackages = new java.util.HashSet<>();
        javaBasePackages.add("java.net");                           // URLConnection, HttpURLConnection
        javaBasePackages.add("sun.net.www.protocol.http");          // HttpURLConnection internal implementation
        javaBasePackages.add("sun.net.www.http");                   // HttpClient internal implementation
        config.put("java.base", Collections.unmodifiableSet(javaBasePackages));

        return Collections.unmodifiableMap(config);
    }

    /**
     * Open necessary module access permissions
     *
     * <p>This method should be called early in the Agent startup to ensure modules are opened
     * before plugin initialization.
     *
     * @param instrumentation Instrumentation instance
     */
    public static void openModules(Instrumentation instrumentation) {
        if (!modulesSupported) {
            LOGGER.fine("Module system not supported (Java 8), skipping module opening.");
            return;
        }

        if (instrumentation == null) {
            LOGGER.warning("Instrumentation is null, cannot open modules!");
            return;
        }

        try {
            LOGGER.info("Opening necessary JDK modules for Sermant plugins...");

            for (Map.Entry<String, Set<String>> entry : MODULE_OPENS_CONFIG.entrySet()) {
                String moduleName = entry.getKey();
                Set<String> packages = entry.getValue();

                openModulePackages(instrumentation, moduleName, packages);
            }

            LOGGER.info("Successfully opened all necessary JDK modules.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to open some modules, some plugins may not work properly on Java 17+: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Open specified packages of a specified module
     *
     * @param instrumentation Instrumentation instance
     * @param moduleName Module name (e.g., "java.base")
     * @param packages Set of packages to be opened
     */
    private static void openModulePackages(Instrumentation instrumentation, String moduleName,
                                           Set<String> packages) {
        try {
            // Get the module with the specified name
            Object targetModule = getModule(moduleName);
            if (targetModule == null) {
                LOGGER.warning("Module [" + moduleName + "] not found, skipping.");
                return;
            }

            // Get the unnamed module (module where Agent and plugins run)
            Object unnamedModule = getUnnamedModule();
            if (unnamedModule == null) {
                LOGGER.warning("Cannot get unnamed module, skipping.");
                return;
            }

            // Use Instrumentation.redefineModule to open package access
            for (String packageName : packages) {
                redefineModule(instrumentation, targetModule, packageName, unnamedModule);
            }

            LOGGER.fine("Successfully opened module [" + moduleName + "] packages: " + packages);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to open module [" + moduleName + "]: " + e.getMessage(), e);
        }
    }

    /**
     * Get the module with the specified name
     *
     * @param moduleName Module name
     * @return Module object, or null if not found
     */
    private static Object getModule(String moduleName) {
        try {
            // ModuleLayer.boot().findModule(moduleName)
            Class<?> moduleLayerClass = Class.forName("java.lang.ModuleLayer");
            Method bootMethod = moduleLayerClass.getMethod("boot");
            Object bootLayer = bootMethod.invoke(null);

            Method findModuleMethod = moduleLayerClass.getMethod("findModule", String.class);
            Object optionalModule = findModuleMethod.invoke(bootLayer, moduleName);

            // Optional.isPresent() && Optional.get()
            Class<?> optionalClass = Class.forName("java.util.Optional");
            Method isPresentMethod = optionalClass.getMethod("isPresent");
            boolean isPresent = (boolean) isPresentMethod.invoke(optionalModule);

            if (isPresent) {
                Method getMethod = optionalClass.getMethod("get");
                return getMethod.invoke(optionalModule);
            }

            return null;
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Failed to get module [" + moduleName + "]: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get the unnamed module (module of the current class loader)
     *
     * @return Unnamed Module object
     */
    private static Object getUnnamedModule() {
        try {
            // ModuleOpener.class.getClassLoader().getUnnamedModule()
            ClassLoader classLoader = ModuleOpener.class.getClassLoader();
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }

            Method getUnnamedModuleMethod = ClassLoader.class.getMethod("getUnnamedModule");
            return getUnnamedModuleMethod.invoke(classLoader);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Failed to get unnamed module: " + e.getMessage());
            return null;
        }
    }

    /**
     * Use Instrumentation.redefineModule to open package access of a module
     *
     * @param instrumentation Instrumentation instance
     * @param module Module to be modified
     * @param packageName Package name to be opened
     * @param targetModule Target module (usually the unnamed module)
     */
    private static void redefineModule(Instrumentation instrumentation, Object module,
                                       String packageName, Object targetModule) {
        try {
            // Build opens parameter: Map<String, Set<Module>>
            Map<String, Set<Object>> extraOpens = new HashMap<>(1);
            Set<Object> targetModules = Collections.singleton(targetModule);
            extraOpens.put(packageName, targetModules);

            // instrumentation.redefineModule(module, extraReads, extraExports, extraOpens, extraUses, extraProvides)
            Method redefineModuleMethod = Instrumentation.class.getMethod(
                    "redefineModule",
                    moduleClass,
                    Set.class,      // extraReads
                    Map.class,      // extraExports
                    Map.class,      // extraOpens
                    Set.class,      // extraUses
                    Map.class       // extraProvides
            );

            redefineModuleMethod.invoke(
                    instrumentation,
                    module,
                    Collections.emptySet(),     // extraReads
                    Collections.emptyMap(),     // extraExports
                    extraOpens,                 // extraOpens - open package access
                    Collections.emptySet(),     // extraUses
                    Collections.emptyMap()      // extraProvides
            );

            LOGGER.fine("Successfully opened package [" + packageName + "] to unnamed module.");
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Failed to open package [" + packageName + "]: " + e.getMessage());
        }
    }

    /**
     * Check whether the module system is supported
     *
     * @return true if running on Java 9+, false if running on Java 8
     */
    public static boolean isModulesSupported() {
        return modulesSupported;
    }
}


/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import com.huawei.apm.core.classloader.PluginClassLoader;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.common.PathIndexer;
import com.huawei.apm.core.serialize.SerializerHolder;

/**
 * 配置储存器
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/8/26
 */
public abstract class ConfigLoader {
    /**
     * 日志
     */
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 配置对象集合，键为配置对象的实现类Class，值为加载完毕的配置对象
     * <p>通过{@link #getConfig(Class)}方法获取配置对象
     */
    private static final Map<String, BaseConfig> CONFIG_MAP = new HashMap<String, BaseConfig>();

    /**
     * 初始化标记
     */
    private static boolean initFlag = false;

    /**
     * 获取配置对象的键
     * <p>如果配置对象被{@link ConfigTypeKey}修饰，取其值
     * <p>如果不被{@link ConfigTypeKey}修饰，则取类的全限定名
     *
     * @param cls 配置对象类
     * @return 前缀字符串
     */
    public static String getTypeKey(Class<?> cls) {
        final ConfigTypeKey configTypeKey = cls.getAnnotation(ConfigTypeKey.class);
        if (configTypeKey == null) {
            return cls.getName();
        } else {
            return configTypeKey.value();
        }
    }

    /**
     * 获取配置信息键
     * <p>通过{@link ConfigFieldKey}注解获取成员属性对应配置信息键
     * <p>不存在注解时，直接取字段名
     *
     * @param field 字段
     * @return 配置信息键
     */
    public static String getFieldKey(Field field) {
        final ConfigFieldKey configFieldKey = field.getAnnotation(ConfigFieldKey.class);
        if (configFieldKey == null) {
            return field.getName();
        } else {
            return configFieldKey.value();
        }
    }

    /**
     * 附带ClassLoader的类型键
     *
     * @param typeKey     类型键
     * @param classLoader ClassLoader
     * @return 附带ClassLoader的类型键
     */
    private static String getCLTypeKey(String typeKey, ClassLoader classLoader) {
        return typeKey + "@" + Integer.toHexString(classLoader.hashCode());
    }

    /**
     * 通过配置对象类型获取配置对象
     *
     * @param cls 配置对象类型
     * @param <R> 配置对象泛型
     * @return 配置对象
     */
    public static <R extends BaseConfig> R getConfig(Class<R> cls) {
        final ClassLoader classLoader = cls.getClassLoader();
        final String typeKey = getTypeKey(cls);
        final String clTypeKey = getCLTypeKey(typeKey, classLoader);
        BaseConfig clConfig = CONFIG_MAP.get(clTypeKey);
        if (clConfig == null || !cls.isAssignableFrom(clConfig.getClass())) {
            final BaseConfig Config = CONFIG_MAP.get(typeKey);
            if (Config != null) {
                if (Config.getClass().getClassLoader() == classLoader) {
                    clConfig = getProxy(Config, classLoader);
                } else {
                    clConfig = getProxy(SerializerHolder.deserialize(SerializerHolder.serialize(Config), cls),
                            classLoader);
                }
                CONFIG_MAP.put(clTypeKey, clConfig);
            } else {
                LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                        "Missing configured instance of [%s], please check.", cls.getName()));
            }
        }
        return (R) clConfig;
    }

    /**
     * 获取配置对象的byte-buddy代理，禁用set方法
     *
     * @param config      源配置对象
     * @param classLoader 用于加载代理类的classloader
     * @return 配置对象的byte-buddy代理
     */
    private static BaseConfig getProxy(final BaseConfig config, ClassLoader classLoader) {
        if (config == null) {
            return null;
        }
        final Class<? extends BaseConfig> configClass = config.getClass();
        final InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                final String methodName = method.getName();
                if (methodName.startsWith("set")) {
                    LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                            "Calling method [%s#%s] is not supported.", configClass, methodName));
                    return null;
                }
                return method.invoke(config, args);
            }
        };
        try {
            return new ByteBuddy().subclass(configClass)
                    .implement(BaseConfig.class)
                    .method(ElementMatchers.<MethodDescription>any())
                    .intercept(InvocationHandlerAdapter.of(handler))
                    .make()
                    .load(classLoader)
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception ignored) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT, "Create proxy of [%s] failed.", configClass));
            return config;
        }
    }

    /**
     * 初始化，将{@code initFlag}修改为真
     *
     * @param agentArgs 启动配置参数
     */
    public static synchronized void initialize(String agentArgs) {
        if (initFlag) {
            return;
        }
        doInitialize(agentArgs);
        initFlag = true;
    }

    /**
     * 执行初始化，主要包含以下步骤：
     * <pre>
     *     1.处理启动配置参数
     *     2.获取加载配置策略
     *     3.加载配置文件
     *     4.查找所有配置对象
     *     5.加载所有配置对象
     *     6.将所有加载完毕的配置对象保留于{@code CONFIG_MAP}中
     * </pre>
     * <p>当加载配置策略不存在时，使用默认的加载策略，将不进行任何配置加载
     * <p>当配置文件不存在时，仅将{@code agentArgs}中的内容处理为配置信息承载对象
     * <p>当部分配置对象封装失败时，将不影响他们保存于{@code CONFIG_MAP}中，也不影响其他配置对象的封装
     *
     * @param agentArgs 启动配置参数
     */
    private static void doInitialize(String agentArgs) {
        final Map<String, String> argsMap = toArgsMap(agentArgs);
        final LoadConfigStrategy<?> loadConfigStrategy = getLoadConfigStrategy();
        final Object holder = loadConfigStrategy.getConfigHolder(PathIndexer.getInstance().getConfigs(), argsMap);
        foreachConfig(new ConfigConsumer() {
            @Override
            public void accept(BaseConfig config) {
                ((LoadConfigStrategy) loadConfigStrategy).loadConfig(holder, config);
                CONFIG_MAP.put(getTypeKey(config.getClass()), config);
            }
        });
    }

    /**
     * 将启动配置参数字符串转换为map
     *
     * @param args 启动配置参数字符串
     * @return 配置参数map
     */
    private static Map<String, String> toArgsMap(String args) {
        final Map<String, String> argsMap = new HashMap<String, String>();
        for (String arg : args.trim().split(",")) {
            final int index = arg.indexOf('=');
            if (index >= 0) {
                argsMap.put(arg.substring(0, index).trim(), arg.substring(index + 1).trim());
            }
        }
        return argsMap;
    }

    /**
     * 通过spi的方式获取加载配置策略
     * <p>需要在{@code META-INF/services}目录中添加加载配置策略{@link LoadConfigStrategy}文件，并键入实现
     * <p>如果声明多个实现，仅第一个有效
     * <p>如果未声明任何实现，使用默认的加载配置策略{@link LoadConfigStrategy.DefaultLoadConfigStrategy}
     * <p>该默认策略不会进行任何业务操作
     *
     * @return 加载配置策略
     */
    private static LoadConfigStrategy<?> getLoadConfigStrategy() {
        final LoadConfigStrategy<?> strategy = PluginClassLoader.getImpl(LoadConfigStrategy.class);
        if (strategy == null) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT, "Missing implement of [%s], use [%s].",
                    LoadConfigStrategy.class.getName(), LoadConfigStrategy.DefaultLoadConfigStrategy.class.getName()));
            return new LoadConfigStrategy.DefaultLoadConfigStrategy();
        }
        return strategy;
    }

    /**
     * 遍历所有通过spi方式声明的配置对象
     * <p>需要在{@code META-INF/services}目录中添加配置基类{@link BaseConfig}文件，并添加实现
     * <p>文件中声明的所有实现都将会进行遍历，每一个实现类都会通过spi获取实例，然后调用{@code configConsumer}进行消费
     *
     * @param configConsumer 配置处理方法
     */
    private static void foreachConfig(ConfigConsumer configConsumer) {
        for (BaseConfig config : PluginClassLoader.load(BaseConfig.class)) {
            configConsumer.accept(config);
        }
    }

    public interface ConfigConsumer {
        void accept(BaseConfig config);
    }
}

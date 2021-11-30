/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.utils;

import com.huawei.javamesh.core.common.LoggerFactory;
import com.huawei.javamesh.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.gray.dubbo.cache.DubboCache;
import com.huawei.gray.dubbo.strategy.TypeStrategy;
import com.huawei.gray.dubbo.strategy.TypeStrategyChooser;
import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.CurrentTag;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;
import com.huawei.route.common.gray.label.entity.Match;
import com.huawei.route.common.gray.label.entity.MatchRule;
import com.huawei.route.common.gray.label.entity.MatchStrategy;
import com.huawei.route.common.gray.label.entity.Route;
import com.huawei.route.common.gray.label.entity.Rule;
import com.huawei.route.common.gray.label.entity.ValueMatch;

import com.alibaba.fastjson.JSONObject;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.remoting.ChannelHandler;
import org.apache.dubbo.remoting.RemotingException;
import org.apache.dubbo.remoting.exchange.ExchangeClient;
import org.apache.dubbo.remoting.exchange.support.header.HeaderExchangeClient;
import org.apache.dubbo.remoting.transport.netty4.NettyClient;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.protocol.AbstractInvoker;
import org.apache.dubbo.rpc.protocol.dubbo.DubboInvoker;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 应用路由插件工具类
 *
 * @author l30008180
 * @since 2021年6月21日
 */
public class RouterUtil {
    private static final String URL_PARA_APP_NAME = "application"; // dubbo请求中应用名

    private static final String REMOTE_APP_NAME = "remote.application"; // dubbo请求中下游应用名

    private static final String URL_PARA_METHOD_NAME = "methods"; // dubbo请求中方法名

    private static final String CLIENT_FIELD_NAME_CLIENT = "client"; // Exchange类中，client字段名称

    private static final String CLIENT_FIELD_NAME_CHANNEL = "channel"; // Exchange类中，channel字段名称

    private static final String CLIENT_FIELD_NAME_HANDLER = "handler"; // Exchange类中，handler字段名称

    private static final String INVOKER_FIELD_NAME_URL = "url"; // Invoker类中，url字段名称

    private static final String INVOKER_FIELD_NAME_INVOKER = "invoker"; // Invoker类中，invoker字段名称

    private static final String INVOKER_FIELD_NAME_ATTACHMENT = "attachment"; // Invoker类中，attachment字段名称

    private static final String INVOKER_FIELD_NAME_MODIFIERS = "modifiers";

    private static final String INVOKER_FIELD_CLIENTS = "clients";

    private static final String CLIENT_FIELD_URL = "url";

    private static final int MAX_RECURS_NUM = 5;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private RouterUtil() {
    }

    /**
     * 根据Dubbo服务地址获取服务名：参数中的application数据
     *
     * @param url dubbo请求地址
     * @return 服务名
     */
    public static String getServiceName(URL url) {
        if (url == null) {
            return "";
        }
        return url.getParameter(URL_PARA_APP_NAME);
    }

    /**
     * 根据Dubbo服务地址获取接口名：参数中的interface数据
     *
     * @param url dubbo请求地址
     * @return 接口名
     */
    public static String getInterfaceName(URL url) {
        return url.getServiceInterface();
    }

    /**
     * 根据Dubbo服务地址获取下游服务名：参数中的remote.application数据
     *
     * @param url dubbo请求地址
     * @return 下游服务名
     */
    public static String getTargetService(URL url) {
        return url.getParameter(REMOTE_APP_NAME);
    }

    /**
     * 根据Dubbo服务地址获取方法：参数中的methods数据
     *
     * @param url dubbo请求地址
     * @return 方法
     */
    public static String getMethodName(URL url) {
        return url.getParameter(URL_PARA_METHOD_NAME);
    }

    /**
     * 根据invocation获取ldc
     *
     * @param invocation invocation
     * @return LDC
     */
    public static String getLdc(Invocation invocation) {
        String ldc = invocation.getAttachment(GrayConstant.GRAY_LDC);
        return StringUtils.isBlank(ldc) ? getLdc() : ldc;
    }

    /**
     * 获取当前服务的ldc
     *
     * @return LDC
     */
    public static String getLdc() {
        GrayConfiguration grayConfiguration = LabelCache.getLabel(DubboCache.getLabelName());
        if (GrayConfiguration.isInValid(grayConfiguration)) {
            return GrayConstant.GRAY_DEFAULT_LDC;
        }
        CurrentTag currentTag = grayConfiguration.getCurrentTag();
        if (currentTag == null || StringUtils.isBlank(currentTag.getLdc())) {
            return GrayConstant.GRAY_DEFAULT_LDC;
        }
        return currentTag.getLdc();
    }

    /**
     * 修改地址
     *
     * @param invocation dubbo invocation
     * @param host 新的地址
     * @param requestUrl 旧的地址
     * @param version 版本
     * @param group 组
     * @param clusterName 集群
     * @throws NoSuchFieldException 异常
     * @throws IllegalAccessException 异常
     */
    public static void changeInvokerClients(Invocation invocation, String host, URL requestUrl, String version,
            String group, String clusterName) throws NoSuchFieldException, IllegalAccessException {
        if (host == null || host.length() == 0 || invocation.getInvoker() == null || requestUrl == null) {
            return;
        }
        DubboInvoker<?> invoker = (DubboInvoker<?>) invocation.getInvoker();
        // 反射获取clients变量
        ExchangeClient[] clients = (ExchangeClient[]) RouterUtil.getField(invoker, INVOKER_FIELD_CLIENTS);
        List<ExchangeClient> clientList = new ArrayList<ExchangeClient>();
        // 获取请求的接口、方法等属性
        String serviceName = RouterUtil.getServiceName(requestUrl);
        String interfaceName = RouterUtil.getInterfaceName(requestUrl);
        for (ExchangeClient client : clients) {
            URL clientUrl;
            try { // 因为类可见性问题，只能用catch异常的方法解决
                clientUrl = (URL) RouterUtil.getField(client, CLIENT_FIELD_URL);
            } catch (NoSuchFieldException e) {
                // 如果没有URL属性，则跳过
                continue;
            } catch (IllegalAccessException e) {
                // 如果没有URL属性，则跳过
                continue;
            }
            // 找到请求接口相同的client，放入列表中
            if (serviceName == null || !serviceName.equals(RouterUtil.getServiceName(clientUrl))) {
                continue;
            }
            if (interfaceName == null || !interfaceName.equals(RouterUtil.getInterfaceName(clientUrl))) {
                continue;
            }
            clientList.add(client);
        }
        // 更换client中的地址
        List<ExchangeClient> buildClients = new ArrayList<ExchangeClient>();
        for (ExchangeClient exchangeClient : clientList) {
            // 更换client中的URL地址
            boolean isChanged = rebuildUrlByIpAndPort(host, exchangeClient, version, group, clusterName);
            if (isChanged) { // 更换失败则不作为将要使用的地址
                buildClients.add(exchangeClient);
            }
            rebuildUrlByIpAndPort(host, invoker, version, group, clusterName);
        }
        setField(invoker, INVOKER_FIELD_CLIENTS, buildClients.toArray(new ExchangeClient[0]));
        changeGroup(invocation, group);
    }

    private static void changeGroup(Invocation invocation, String group) {
        if (StringUtils.isBlank(group)) {
            invocation.getAttachments().remove(GrayConstant.URL_GROUP_KEY);
        } else {
            invocation.getAttachments().put(GrayConstant.URL_GROUP_KEY, group);
        }
    }

    /**
     * 更换旧的URL中的IP和端口，并生成新的URL对象返回
     *
     * @param host 新的请求地址
     * @param invoker 地址触发
     * @param version 版本
     * @param group 组名
     * @param clusterName 集群名
     */
    public static void rebuildUrlByIpAndPort(String host, Invoker<?> invoker, String version, String group,
            String clusterName) {
        URL validUrl = getValidUrl(host);
        if (validUrl == null) {
            return;
        }
        String ip = validUrl.getHost();
        int port = validUrl.getPort();
        URL oldUrl = invoker.getUrl();
        Map<String, String> map = getNewParameters(oldUrl.getParameters(), version, group, clusterName);
        // 新建一个请求地址，更换为路由Server中的IP和端口
        URL url = new URL(oldUrl.getProtocol(), oldUrl.getUsername(), oldUrl.getPassword(), ip,
                port == 0 ? oldUrl.getPort() : port, oldUrl.getPath(), map);
        try {
            AbstractInvoker<?> targetInvoker = getTargetInvoker(invoker);
            setField(AbstractInvoker.class, targetInvoker, INVOKER_FIELD_NAME_URL, url);
            Map<String, String> attachment = new HashMap<String, String>((Map<String, String>) getField(targetInvoker,
                    INVOKER_FIELD_NAME_ATTACHMENT));
            if (StringUtils.isBlank(url.getParameter(GrayConstant.URL_GROUP_KEY))) {
                attachment.remove(GrayConstant.URL_GROUP_KEY);
            } else {
                attachment.put(GrayConstant.URL_GROUP_KEY, url.getParameter(GrayConstant.URL_GROUP_KEY));
            }
            setField(AbstractInvoker.class, targetInvoker, INVOKER_FIELD_NAME_ATTACHMENT,
                    Collections.unmodifiableMap(attachment));
            if (invoker instanceof DubboInvoker) {
                setField(DubboInvoker.class, (DubboInvoker<?>) invoker, GrayConstant.URL_VERSION_KEY,
                        url.getParameter(GrayConstant.URL_VERSION_KEY, GrayConstant.DUBBO_DEFAULT_VERSION));
            }
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "error to setField!", e);
        } catch (NoSuchFieldException e) {
            LOGGER.log(Level.SEVERE, "error to setField!", e);
        }
    }

    /**
     * 更换旧的URL中的IP和端口，并生成新的URL对象返回
     *
     * @param host 新的请求地址
     * @param client 地址触发
     * @param version 版本
     * @param group 组名
     * @param clusterName 集群名
     * @return 是否更换成功
     */
    public static boolean rebuildUrlByIpAndPort(String host, ExchangeClient client, String version, String group,
            String clusterName) {
        URL validUrl = getValidUrl(host);
        if (validUrl == null) {
            return false;
        }
        String ip = validUrl.getHost();
        int port = validUrl.getPort();
        try {
            URL oldUrl = (URL) getField(client, INVOKER_FIELD_NAME_URL);
            Map<String, String> map = getNewParameters(oldUrl.getParameters(), version, group, clusterName);
            // 新建一个请求地址，更换为路由Server中的IP和端口
            URL url = new URL(oldUrl.getProtocol(), oldUrl.getUsername(), oldUrl.getPassword(), ip,
                    port == 0 ? oldUrl.getPort() : port, oldUrl.getPath(), map);
            changeClientRequest(client, url, 0);
            return true;
        } catch (IllegalAccessException e) {
            return false;
        } catch (NoSuchFieldException e) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private static Map<String, String> getNewParameters(Map<String, String> oldParameters, String version, String group,
            String clusterName) {
        Map<String, String> map = new HashMap<String, String>();
        if (oldParameters != null) {
            map.putAll(oldParameters);
        }
        if (StringUtils.isNotBlank(version)) {
            map.put(GrayConstant.URL_VERSION_KEY, version);
        } else {
            map.remove(GrayConstant.URL_VERSION_KEY);
        }
        if (StringUtils.isNotBlank(group)) {
            map.put(GrayConstant.URL_GROUP_KEY, group);
        } else {
            map.remove(GrayConstant.URL_GROUP_KEY);
        }
        if (StringUtils.isNotBlank(clusterName)) {
            map.put(GrayConstant.URL_CLUSTER_NAME_KEY, clusterName);
        } else {
            map.remove(GrayConstant.URL_CLUSTER_NAME_KEY);
        }
        return map;
    }

    private static URL getValidUrl(String host) {
        if (host == null || host.trim().length() == 0) {
            return null;
        }
        try {
            URL url = URL.valueOf(host);
            String ip = url.getHost();
            if (ip == null || ip.trim().length() == 0) {
                // 如果产生空数据，则不再进行替换
                return null;
            }
            return url;
        } catch (IllegalArgumentException exception) {
            // 如果转换失败，就不更换原来的地址
            return null;
        }
    }

    private static void changeClientRequest(ExchangeClient client, URL newUrl, int counter)
            throws NoSuchFieldException, IllegalAccessException, IOException {
        if (counter > MAX_RECURS_NUM) { // 防止出现无限递归
            return;
        }
        if (client instanceof HeaderExchangeClient) {
            // 修改其中channel的地址
            Object headerExchangeClient = getField(client, CLIENT_FIELD_NAME_CHANNEL);
            NettyClient channel = (NettyClient) getField(headerExchangeClient, CLIENT_FIELD_NAME_CHANNEL);
            URL url = channel.getUrl();
            // 如果地址相同，则不需要更换
            if (url.getHost().equals(newUrl.getHost()) && url.getPort() == newUrl.getPort()
                    && getServiceName(url).equals(getServiceName(newUrl))
                    && getInterfaceName(url).equals(getInterfaceName(newUrl))
                    && getMethodName(url).equals(getMethodName(newUrl))) {
                return;
            }
            ChannelHandler handler = getField(NettyClient.class, ChannelHandler.class, channel,
                    CLIENT_FIELD_NAME_HANDLER);
            try {
                NettyClient newNettyClient = new NettyClient(newUrl, handler);
                // 替换client类型
                setField(headerExchangeClient, CLIENT_FIELD_NAME_CHANNEL, newNettyClient);
                // 关闭原链接
                channel.close();
            } catch (RemotingException e) {
                LOGGER.log(Level.SEVERE, "error to create NettyClient!", e);
            }
        } else {
            // 当类型为ReferenceCountExchangeClient或者LazyConnectExchangeClient时
            try {
                changeClientUrl(client, newUrl);
            } catch (NoSuchFieldException e) {
                return;
            } catch (IllegalAccessException e) {
                return;
            }
            ExchangeClient exchangeClient = (ExchangeClient) getField(client, CLIENT_FIELD_NAME_CLIENT);
            changeClientRequest(exchangeClient, newUrl, counter + 1);
        }
    }

    private static void changeClientUrl(ExchangeClient client, URL newUrl)
            throws NoSuchFieldException, IllegalAccessException {
        setField(client, INVOKER_FIELD_NAME_URL, newUrl);
    }

    /**
     * 利用递归的方式找出多层封装的invoker
     *
     * @param invoker 处理类
     * @return 处理类
     * @throws NoSuchFieldException 可能会抛出的异常
     * @throws IllegalAccessException 可能会抛出的异常
     */
    public static AbstractInvoker<?> getTargetInvoker(Invoker<?> invoker)
            throws NoSuchFieldException, IllegalAccessException {
        if (invoker instanceof AbstractInvoker) {
            return (AbstractInvoker<?>) invoker;
        }
        Field subInvokerField = getFieldFromObject(invoker.getClass(), INVOKER_FIELD_NAME_INVOKER);
        setAccessible(subInvokerField);
        return getTargetInvoker((Invoker<?>) subInvokerField.get(invoker));
    }

    /**
     * 利用反射重置成员变量的值
     *
     * @param clazz 类型
     * @param target 需要修改成员变量的实例
     * @param fieldName 成员变量名称
     * @param fieldValue 成员变量值
     * @param <C> 类型
     * @param <V> 成员变量类型
     * @throws IllegalAccessException 可能会抛出反射异常
     * @throws NoSuchFieldException 可能会抛出反射异常
     */
    public static <C, V> void setField(Class<C> clazz, C target, String fieldName, V fieldValue)
            throws IllegalAccessException, NoSuchFieldException {
        Field field = getFieldFromObject(clazz, fieldName);
        setAccessible(field);
        setModifiable(field);
        field.set(target, fieldValue);
    }

    /**
     * 利用反射重置成员变量的值
     *
     * @param target 需要修改成员变量的实例
     * @param fieldName 成员变量名称
     * @param fieldValue 成员变量值
     * @param <C> 类型
     * @param <V> 成员变量类型
     * @throws NoSuchFieldException 可能会抛出反射异常
     * @throws IllegalAccessException 可能会抛出反射异常
     */
    public static <C, V> void setField(C target, String fieldName, V fieldValue)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = getFieldFromObject(target.getClass(), fieldName);
        setAccessible(field);
        setModifiable(field);
        field.set(target, fieldValue);
    }

    /**
     * 利用反射获取成员变量的值
     *
     * @param clazzObj 类的实例类型
     * @param fieldValue 成员变量值类型
     * @param target 需要修改成员变量的实例
     * @param fieldName 成员变量名称
     * @param <C> 类型
     * @param <V> 成员变量值类型
     * @return 成员变量的值
     * @throws IllegalAccessException 可能会抛出反射异常
     * @throws NoSuchFieldException 可能会抛出反射异常
     * @throws ClassCastException 可能会抛出转换异常
     */
    public static <C, V> V getField(Class<C> clazzObj, Class<V> fieldValue, C target, String fieldName)
            throws IllegalAccessException, NoSuchFieldException, ClassCastException {
        Field field = getFieldFromObject(clazzObj, fieldName);
        setAccessible(field);
        return fieldValue.cast(field.get(target));
    }

    /**
     * 利用反射获取成员变量的值
     *
     * @param target 需要修改成员变量的实例
     * @param fieldName 成员变量名称
     * @param <C> 类型
     * @return 成员变量的值
     * @throws IllegalAccessException 可能会抛出反射异常
     * @throws NoSuchFieldException 可能会抛出反射异常
     */
    public static <C> Object getField(C target, String fieldName) throws IllegalAccessException, NoSuchFieldException {
        Field field = getFieldFromObject(target.getClass(), fieldName);
        setAccessible(field);
        return field.get(target);
    }

    /**
     * 将private修饰符去掉
     *
     * @param field 需要设置的成员变量
     */
    private static void setAccessible(Field field) {
        // 如果是Final修饰符修饰的变量，需要将final修饰符去掉
        field.setAccessible(true);
    }

    /**
     * 将final修饰符去掉
     *
     * @param field 需要设置的成员变量
     * @throws IllegalAccessException 可能会抛出反射异常
     * @throws NoSuchFieldException 可能会抛出反射异常
     */
    private static void setModifiable(Field field) throws NoSuchFieldException, IllegalAccessException {
        // 如果是Final修饰符修饰的变量，需要将final修饰符去掉
        Field modifiersField = Field.class.getDeclaredField(INVOKER_FIELD_NAME_MODIFIERS);
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    /**
     * 获取指定的成员变量（包括父类）
     *
     * @param clazz 类型
     * @param fieldName 成员变量名称
     * @param <C> 类型
     * @return 成员变量的反射类型
     * @throws NoSuchFieldException 未找到时的异常
     */
    private static <C> Field getFieldFromObject(Class<C> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> currClass = clazz;
        do {
            try {
                return currClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currClass = currClass.getSuperclass();
            }
        } while (!clazz.equals(Object.class));
        throw new NoSuchFieldException();
    }

    /**
     * 获取合法的目标规则
     *
     * @param grayConfiguration 标签
     * @param targetService 目标服务
     * @param interfaceName 接口
     * @return 目标规则
     */
    public static List<Rule> getValidRules(GrayConfiguration grayConfiguration, String targetService,
            String interfaceName) {
        if (GrayConfiguration.isInValid(grayConfiguration)) {
            return Collections.emptyList();
        }
        Map<String, List<Rule>> routeRule = grayConfiguration.getRouteRule();
        if (CollectionUtils.isEmpty(routeRule) || CollectionUtils.isEmpty(routeRule.get(targetService))) {
            return Collections.emptyList();
        }
        List<Rule> list = new ArrayList<Rule>();
        for (Rule rule : routeRule.get(targetService)) {
            if (!isValidRule(rule, interfaceName)) {
                continue;
            }

            // 去掉无效的规则
            removeInValidRules(rule.getMatch().getArgs());

            // 去掉无效的路由
            removeInValidRoute(rule.getRoute());
            list.add(rule);
        }
        Collections.sort(list, new Comparator<Rule>() {
            @Override
            public int compare(Rule o1, Rule o2) {
                return o1.getPrecedence() - o2.getPrecedence();
            }
        });
        return list;
    }

    private static void removeInValidRules(Map<String, List<MatchRule>> args) {
        Iterator<Entry<String, List<MatchRule>>> matchRuleListIterator = args.entrySet().iterator();
        while (matchRuleListIterator.hasNext()) {
            if (isInValidArgs(matchRuleListIterator.next())) {
                matchRuleListIterator.remove();
            }
        }
        for (List<MatchRule> matchRules : args.values()) {
            Iterator<MatchRule> matchRuleIterator = matchRules.iterator();
            while (matchRuleIterator.hasNext()) {
                if (isInValidMatchRule(matchRuleIterator.next())) {
                    matchRuleIterator.remove();
                }
            }
        }
    }

    private static void removeInValidRoute(List<Route> routeList) {
        Iterator<Route> routeIterator = routeList.iterator();
        while (routeIterator.hasNext()) {
            if (isInValidRoute(routeIterator.next())) {
                routeIterator.remove();
            }
        }
    }

    private static boolean isValidRule(Rule rule, String interfaceName) {
        if (rule == null) {
            return false;
        }
        Match match = rule.getMatch();
        if (match == null) {
            return false;
        }
        String source = match.getSource();
        if (StringUtils.isNotBlank(source) && !source.equals(DubboCache.getAppName())) {
            return false;
        }
        if (!interfaceName.equals(match.getPath())) {
            return false;
        }
        if (CollectionUtils.isEmpty(match.getArgs())) {
            return false;
        }
        return !CollectionUtils.isEmpty(rule.getRoute());
    }

    private static boolean isInValidArgs(Entry<String, List<MatchRule>> entry) {
        return StringUtils.isBlank(entry.getKey()) || CollectionUtils.isEmpty(entry.getValue());
    }

    private static boolean isInValidMatchRule(MatchRule matchRule) {
        return matchRule == null || matchRule.getValueMatch() == null
                || CollectionUtils.isEmpty(matchRule.getValueMatch().getValues())
                || matchRule.getValueMatch().getMatchStrategy() == null;
    }

    private static boolean isInValidRoute(Route route) {
        return route == null || route.getTags() == null || StringUtils.isBlank(route.getTags().getVersion());
    }

    /**
     * 获取匹配的路由
     *
     * @param list 有效的规则
     * @param arguments dubbo的参数
     * @return 匹配的路由
     */
    public static List<Route> getRoutes(List<Rule> list, Object[] arguments) {
        for (Rule rule : list) {
            List<Route> routeList = getRoutes(arguments, rule);
            if (routeList != null) {
                return routeList;
            }
        }
        return null;
    }

    private static List<Route> getRoutes(Object[] arguments, Rule rule) {
        Match match = rule.getMatch();
        boolean fullMatch = match.isFullMatch();
        Map<String, List<MatchRule>> args = match.getArgs();
        for (Entry<String, List<MatchRule>> entry : args.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(GrayConstant.DUBBO_SOURCE_TYPE_PREFIX)) {
                continue;
            }
            List<MatchRule> matchRuleList = entry.getValue();
            for (MatchRule matchRule : matchRuleList) {
                ValueMatch valueMatch = matchRule.getValueMatch();
                List<String> values = valueMatch.getValues();
                MatchStrategy matchStrategy = valueMatch.getMatchStrategy();
                String arg = getArg(matchRule.getType(), key, arguments);
                if (!fullMatch && matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 如果不是全匹配，且匹配了一个，那么直接return
                    return rule.getRoute();
                }
                if (fullMatch && !matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 如果是全匹配，且有一个不匹配，则继续下一个规则
                    return null;
                }
            }
        }
        if (fullMatch) {
            // 如果是全匹配，走到这里，说明没有不匹配的，直接return
            return rule.getRoute();
        }
        // 如果不是全匹配，走到这里，说明没有一个规则能够匹配上，则继续下一个规则
        return null;
    }

    private static String getArg(String type, String key, Object[] arguments) {
        if (arguments == null) {
            return null;
        }
        TypeStrategy typeStrategy = TypeStrategyChooser.INSTANCE.choose(type);
        if (typeStrategy == null) {
            return null;
        }
        int index;
        try {
            index = Integer.parseInt(key.substring(GrayConstant.DUBBO_SOURCE_TYPE_PREFIX.length()));
        } catch (NumberFormatException e) {
            LOGGER.warning("Source type " + key + " is invalid.");
            return null;
        }
        if (index < 0 || index >= arguments.length || arguments[index] == null) {
            return null;
        }
        return typeStrategy.getValue(arguments[index], type);
    }

    /**
     * 获取目标地址并设置attachments
     *
     * @param instances 实例
     * @param invocation invocation
     * @param version tag
     * @param ldc ldc
     * @return ip:port
     */
    public static String getTargetAndSetAttachment(Instances instances, Invocation invocation, String version,
            String ldc) {
        if (instances != null && instances.getIp() != null && instances.getPort() > 0) {
            CurrentTag currentTag = new CurrentTag();
            currentTag.setVersion(version);
            currentTag.setLdc(ldc);
            invocation.getAttachments().put(GrayConstant.GRAY_TAG, JSONObject.toJSONString(currentTag));
            if (!invocation.getAttachments().containsKey(GrayConstant.GRAY_LDC)) {
                invocation.getAttachments().put(GrayConstant.GRAY_LDC, RouterUtil.getLdc(invocation));
            }
            return instances.getIp() + ":" + instances.getPort();
        }
        return null;
    }
}
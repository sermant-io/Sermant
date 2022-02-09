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

package com.huawei.sermant.core.plugin.agent.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 插件的执行上下文，封装拦截器运作所需的所有参数
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-25
 */
public class ExecuteContext {
    /**
     * 被增强的类
     */
    private final Class<?> rawCls;
    /**
     * 被增强的构造函数，注意：增强方法时为空
     */
    private final Constructor<?> constructor;
    /**
     * 被增强的方法，注意：增强构造函数时为空
     */
    private final Method method;
    /**
     * 被增强的对象，注意：
     * <pre>
     *     1.增强静态方法时为空
     *     2.增强构造函数时，前置方法的执行上下文中，该值为空
     * </pre>
     */
    private Object object;
    /**
     * 被增强的方法入参
     */
    private Object[] arguments;

    /**
     * 是否跳过被增强方法的主要流程
     */
    private boolean isSkip;

    /**
     * 被增强方法调用的结果
     */
    private Object result;

    /**
     * 被增强方法调用过程中抛出的异常
     */
    private Throwable throwable;

    /**
     * 额外的静态属性
     */
    private Map<String, Object> extStaticFields;

    /**
     * 额外的成员属性
     */
    private Map<String, Object> extMemberFields;

    /**
     * 贯穿调用流程的本地局部属性集
     */
    private Map<String, Object> localFields;

    /**
     * 原生字段集，每次获取的字段都会暂时保存在此
     */
    private Map<String, Field> rawFields;

    @SuppressWarnings("checkstyle:ParameterNumber")
    private ExecuteContext(Object object, Class<?> rawCls, Constructor<?> constructor, Method method,
            Object[] arguments, Map<String, Object> extStaticFields, Map<String, Object> extMemberFields) {
        this.object = object;
        this.rawCls = rawCls;
        this.constructor = constructor;
        this.method = method;
        this.arguments = arguments;
        this.isSkip = false;
        this.result = null;
        this.throwable = null;
        this.extStaticFields = extStaticFields;
        this.extMemberFields = extMemberFields;
        this.localFields = null;
    }

    /**
     * 创建构造函数的执行上下文
     *
     * @param cls             被增强的类
     * @param constructor     被增强的构造函数
     * @param arguments       构造函数入参
     * @param extStaticFields 额外的静态属性集
     * @return 执行上下文
     */
    public static ExecuteContext forConstructor(Class<?> cls, Constructor<?> constructor, Object[] arguments,
            Map<String, Object> extStaticFields) {
        return new ExecuteContext(null, cls, constructor, null, arguments, extStaticFields, null);
    }

    /**
     * 创建成员方法的执行上下文
     *
     * @param object          被增强的对象
     * @param method          被增强的方法
     * @param arguments       方法的入参
     * @param extStaticFields 额外的静态属性集
     * @param extMemberFields 额外的成员属性集
     * @return 执行上下文
     */
    public static ExecuteContext forMemberMethod(Object object, Method method, Object[] arguments,
            Map<String, Object> extStaticFields, Map<String, Object> extMemberFields) {
        return new ExecuteContext(object, object.getClass(), null, method, arguments, extStaticFields, extMemberFields);
    }

    /**
     * 构建静态方法的执行上下文
     *
     * @param cls             被增强的类
     * @param method          被增强的方法
     * @param arguments       方法的入参
     * @param extStaticFields 额外的静态属性集
     * @return 执行上下文
     */
    public static ExecuteContext forStaticMethod(Class<?> cls, Method method, Object[] arguments,
            Map<String, Object> extStaticFields) {
        return new ExecuteContext(null, cls, null, method, arguments, extStaticFields, null);
    }

    /**
     * 适配增强构造函数时的后置触发点
     *
     * @param thisObj             构造的对象
     * @param thisExtMemberFields 成员属性集
     * @return 执行上下文
     */
    public ExecuteContext afterConstructor(Object thisObj, Map<String, Object> thisExtMemberFields) {
        this.object = thisObj;
        this.extMemberFields = thisExtMemberFields;
        return this;
    }

    /**
     * 适配增强静态方法和成员方法时的后置触发点
     *
     * @param methodResult    方法主要流程结果
     * @param methodThrowable 方法主要流程异常
     * @return 执行上下文
     */
    public ExecuteContext afterMethod(Object methodResult, Throwable methodThrowable) {
        this.result = methodResult;
        this.throwable = methodThrowable;
        return this;
    }

    public Object getObject() {
        return object;
    }

    public Class<?> getRawCls() {
        return rawCls;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public boolean isSkip() {
        return isSkip;
    }

    public Object getResult() {
        return result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Map<String, Object> getExtStaticFields() {
        return extStaticFields;
    }

    public Map<String, Object> getExtMemberFields() {
        return extMemberFields;
    }

    /**
     * 检索属性，静态和成员属性都在此检索，仅检索被增强类定义的属性及其公有的属性，超类protected的属性将不会被获取
     *
     * @param fieldName 属性名称
     * @return 获取的属性
     * @throws NoSuchFieldException 获取属性异常
     */
    private Field searchField(String fieldName) throws NoSuchFieldException {
        Field field;
        try {
            field = rawCls.getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
        } catch (NoSuchFieldException ignored) {
            field = rawCls.getField(fieldName);
        }
        return field;
    }

    /**
     * 获取属性，从{@link #searchField}中检索的属性将被临时存放到{@link #rawFields}中，以供下次获取
     * <p>仅在一个被增强方法的调用过程中生效
     *
     * @param fieldName 属性名
     * @return 获取的属性
     * @throws NoSuchFieldException 获取属性异常
     */
    private Field getField(String fieldName) throws NoSuchFieldException {
        Field field;
        if (rawFields == null) {
            field = searchField(fieldName);
            rawFields = new HashMap<>();
            rawFields.put(fieldName, field);
        } else {
            field = rawFields.get(fieldName);
            if (field == null) {
                field = searchField(fieldName);
                rawFields.put(fieldName, field);
            }
        }
        return field;
    }

    /**
     * 获取静态属性，见{@link #getField}
     *
     * @param fieldName 属性名
     * @return 获取的属性
     * @throws NoSuchFieldException 获取属性异常
     */
    private Field getStaticField(String fieldName) throws NoSuchFieldException {
        final Field field = getField(fieldName);
        if (Modifier.isStatic(field.getModifiers())) {
            return field;
        }
        throw new NoSuchFieldException();
    }

    /**
     * 获取成员属性，见{@link #getField}
     *
     * @param fieldName 属性名
     * @return 获取的属性
     * @throws NoSuchFieldException 获取属性异常
     */
    private Field getMemberField(String fieldName) throws NoSuchFieldException {
        final Field field = getField(fieldName);
        if (Modifier.isStatic(field.getModifiers())) {
            throw new NoSuchFieldException();
        }
        return field;
    }

    /**
     * 设置原生静态属性值
     *
     * @param fieldName 属性名
     * @param value     属性值
     * @throws NoSuchFieldException   找不到该属性
     * @throws IllegalAccessException 属性访问失败
     */
    public void setRawStaticFieldValue(String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        getStaticField(fieldName).set(null, value);
    }

    /**
     * 设置额外静态属性值
     *
     * @param fieldName 属性名
     * @param value     属性值
     */
    public void setExtStaticFieldValue(String fieldName, Object value) {
        if (extStaticFields == null) {
            extStaticFields = new HashMap<>();
        }
        extStaticFields.put(fieldName, value);
    }

    /**
     * 设置静态属性值，原生静态属性不存在时，写入额外静态属性集中
     *
     * @param fieldName 属性名
     * @param value     属性值
     */
    public void setStaticFieldValue(String fieldName, Object value) {
        try {
            setRawStaticFieldValue(fieldName, value);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            setExtStaticFieldValue(fieldName, value);
        }
    }

    /**
     * 获取原生静态属性值
     *
     * @param fieldName 属性名
     * @return 属性值
     * @throws NoSuchFieldException   找不到该属性
     * @throws IllegalAccessException 属性访问失败
     */
    public Object getRawStaticFieldValue(String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        return getStaticField(fieldName).get(null);
    }

    /**
     * 获取额外静态属性值
     *
     * @param fieldName 属性名
     * @return 属性值
     */
    public Object getExtStaticFieldValue(String fieldName) {
        return extStaticFields == null ? null : extStaticFields.get(fieldName);
    }

    /**
     * 获取静态属性值，原生静态属性不存在时，从额外静态属性中获取
     *
     * @param fieldName 属性名
     * @return 属性值
     */
    public Object getStaticFieldValue(String fieldName) {
        try {
            return getRawStaticFieldValue(fieldName);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return getExtStaticFieldValue(fieldName);
        }
    }

    /**
     * 设置原生成员属性值
     *
     * @param fieldName 属性名
     * @param value     属性值
     * @throws NoSuchFieldException   找不到该属性
     * @throws IllegalAccessException 属性访问失败
     */
    public void setRawMemberFieldValue(String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        getMemberField(fieldName).set(null, value);
    }

    /**
     * 设置额外成员属性值
     *
     * @param fieldName 属性名
     * @param value     属性值
     */
    public void setExtMemberFieldValue(String fieldName, Object value) {
        if (extMemberFields == null) {
            extMemberFields = new HashMap<>();
        }
        extMemberFields.put(fieldName, value);
    }

    /**
     * 设置成员属性值，原生成员属性不存在时，写入额外成员属性集中
     *
     * @param fieldName 属性名
     * @param value     属性值
     */
    public void setMemberFieldValue(String fieldName, Object value) {
        if (object == null) {
            throw new UnsupportedOperationException(
                    "It's not allowed to operate member field when enhancing static method or entering constructor. ");
        }
        try {
            setRawMemberFieldValue(fieldName, value);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            setExtMemberFieldValue(fieldName, value);
        }
    }

    /**
     * 获取原生成员属性值
     *
     * @param fieldName 属性名
     * @return 属性值
     * @throws NoSuchFieldException   找不到该属性
     * @throws IllegalAccessException 属性访问失败
     */
    public Object getRawMemberFieldValue(String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        return getMemberField(fieldName).get(object);
    }

    /**
     * 获取额外成员属性值
     *
     * @param fieldName 属性名
     * @return 属性值
     */
    public Object getExtMemberFieldValue(String fieldName) {
        return extMemberFields == null ? null : extMemberFields.get(fieldName);
    }

    /**
     * 获取成员属性值，原生成员属性不存在时，从额外成员属性中获取
     *
     * @param fieldName 属性名
     * @return 属性值
     */
    public Object getMemberFieldValue(String fieldName) {
        if (object == null) {
            throw new UnsupportedOperationException(
                    "It's not allowed to operate member field when enhancing static method or entering constructor. ");
        }
        try {
            return getRawMemberFieldValue(fieldName);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return getExtMemberFieldValue(fieldName);
        }
    }

    /**
     * 获取局部属性集
     *
     * @return 局部属性集
     */
    private Map<String, Object> getLocalFields() {
        if (localFields == null) {
            localFields = new HashMap<>();
        }
        return localFields;
    }

    /**
     * 设置局部属性值
     *
     * @param fieldName 属性名
     * @param value     属性值
     */
    public void setLocalFieldValue(String fieldName, Object value) {
        getLocalFields().put(fieldName, value);
    }

    /**
     * 获取局部属性值
     *
     * @param fieldName 属性名
     * @return 属性值
     */
    public Object getLocalFieldValue(String fieldName) {
        return getLocalFields().get(fieldName);
    }

    /**
     * 修改入参，注意：该方法不做入参检查，需要使用者确保入参数量和类型是否正确
     *
     * @param fixedArguments 入参集
     * @return 执行上下文
     */
    public ExecuteContext changeArgs(Object[] fixedArguments) {
        this.arguments = fixedArguments;
        return this;
    }

    /**
     * 跳过主要流程，并设置最终方法结果，注意，增强构造函数时，不能跳过主要流程
     *
     * @param fixedResult 修正的方法结果
     * @return 执行上下文
     */
    public ExecuteContext skip(Object fixedResult) {
        if (method == null) {
            throw new UnsupportedOperationException("Skip method is not support when enhancing constructor. ");
        }
        this.isSkip = true;
        this.result = fixedResult;
        return this;
    }

    /**
     * 修改结果，注意，该方法不会校验结果的类型，需要使用者自行判断
     *
     * @param fixedResult 修正的结果
     * @return 执行上下文
     */
    public ExecuteContext changeResult(Object fixedResult) {
        if (method == null) {
            throw new UnsupportedOperationException("Change result method is not support when enhancing constructor. ");
        }
        this.result = fixedResult;
        return this;
    }

    @Override
    public String toString() {
        return "ExecuteContext{"
                + "rawCls=" + rawCls
                + ", constructor=" + constructor
                + ", method=" + method
                + ", object=" + object
                + ", arguments=" + Arrays.toString(arguments)
                + ", isSkip=" + isSkip
                + ", result=" + result
                + ", throwable=" + throwable
                + ", extStaticFields=" + extStaticFields
                + ", extMemberFields=" + extMemberFields
                + ", localFields=" + localFields
                + ", rawFields=" + rawFields
                + '}';
    }
}

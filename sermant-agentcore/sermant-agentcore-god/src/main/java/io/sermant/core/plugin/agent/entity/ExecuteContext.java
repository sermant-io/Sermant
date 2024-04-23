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

package io.sermant.core.plugin.agent.entity;

import io.sermant.core.plugin.agent.interceptor.Interceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

/**
 * The execution context of the Interceptor, encapsulates all the parameters needed for the interceptor operation
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-25
 */
public class ExecuteContext {
    /**
     * enhanced class
     */
    private final Class<?> rawCls;

    /**
     * Enhanced constructor, null if enhance method
     */
    private final Constructor<?> constructor;

    /**
     * Enhanced method, null if enhance constructor
     */
    private final Method method;

    /**
     * Enhanced object, note:
     * <pre>
     *     1.Null when enhance static methods
     *     2.In the execution context of the preceding trigger point method, object is null when enhance constructors
     * </pre>
     */
    private Object object;

    /**
     * arguments of enhanced method
     */
    private Object[] arguments;

    /**
     * Whether to skip the main flow of the enhanced method
     */
    private boolean isSkip;

    /**
     * Whether to change exception thrown by method
     */
    private boolean isChangeThrowable;

    /**
     * The return result of the enhanced method
     */
    private Object result;

    /**
     * The exception thrown during invocation to the enhanced method
     */
    private Throwable throwable;

    /**
     * The exception thrown to the host instance
     */
    private Throwable throwableOut;

    /**
     * Additional static fields throughout the execution context procedure
     */
    private Map<String, Object> extStaticFields;

    /**
     * Additional member fields throughout the execution context procedure
     */
    private Map<String, Object> extMemberFields;

    /**
     * Map of local fields throughout the execution context procedure
     */
    private Map<String, Object> localFields;

    /**
     * Interceptor bidirectional iterator
     */
    private ListIterator<Interceptor> interceptorIterator;

    /**
     * Map of raw fields, where each fetched field is temporarily stored
     */
    private Map<String, Field> rawFields;

    private ExecuteContext(Object object, Class<?> rawCls, Constructor<?> constructor, Method method,
            Object[] arguments) {
        this.object = object;
        this.rawCls = rawCls;
        this.constructor = constructor;
        this.method = method;
        this.arguments = arguments;
    }

    /**
     * Create execution context for constructor
     *
     * @param cls The enhanced class
     * @param constructor The enhanced constructor
     * @param arguments Constructor input arguments
     * @param extStaticFields Additional static fields
     * @return ExecuteContext
     */
    public static ExecuteContext forConstructor(Class<?> cls, Constructor<?> constructor, Object[] arguments,
            Map<String, Object> extStaticFields) {
        return new ExecuteContext(null, cls, constructor, null, arguments);
    }

    /**
     * Creates execution context for member method
     *
     * @param object Enhanced object
     * @param method Enhanced method
     * @param arguments method arguments
     * @param extStaticFields Additional static fields
     * @param extMemberFields Additional member fields
     * @return ExecuteContext
     */
    public static ExecuteContext forMemberMethod(Object object, Method method, Object[] arguments,
            Map<String, Object> extStaticFields, Map<String, Object> extMemberFields) {
        return new ExecuteContext(object, object.getClass(), null, method, arguments);
    }

    /**
     * Creates execution context for static method
     *
     * @param cls Enhanced class
     * @param method Enhanced method
     * @param arguments method arguments
     * @param extStaticFields Additional static fields
     * @return ExecuteContext
     */
    public static ExecuteContext forStaticMethod(Class<?> cls, Method method, Object[] arguments,
            Map<String, Object> extStaticFields) {
        return new ExecuteContext(null, cls, null, method, arguments);
    }

    /**
     * The post-trigger point when adapting enhanced constructor
     *
     * @param thisObj constructed object
     * @param thisExtMemberFields Member fields
     * @return ExecuteContext
     */
    public ExecuteContext afterConstructor(Object thisObj, Map<String, Object> thisExtMemberFields) {
        this.object = thisObj;
        this.extMemberFields = thisExtMemberFields;
        return this;
    }

    /**
     * The post-trigger point when adapting the enhanced static method and member method
     *
     * @param methodResult method result
     * @param methodThrowable method throwable
     * @return ExecuteContext
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

    public boolean isChangeThrowable() {
        return isChangeThrowable;
    }

    public Object getResult() {
        return result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public Throwable getThrowableOut() {
        return throwableOut;
    }

    public Map<String, Object> getExtStaticFields() {
        return extStaticFields;
    }

    public Map<String, Object> getExtMemberFields() {
        return extMemberFields;
    }

    public ListIterator<Interceptor> getInterceptorIterator() {
        return interceptorIterator;
    }

    public void setInterceptorIterator(
            ListIterator<Interceptor> interceptorIterator) {
        this.interceptorIterator = interceptorIterator;
    }

    /**
     * Both static and member field are retrieved here. Only fields defined by the enhanced class and their public
     * fields are retrieved. Protected fields of the superclass will not be retrieved
     *
     * @param fieldName field name
     * @return Field
     * @throws NoSuchFieldException Get field exception
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
     * Get field. Fields retrieved from {@link #searchField} are temporarily stored in {@link #rawFields} for next
     * retrieval
     * <p>Takes effect only during a call to an enhanced method
     *
     * @param fieldName field name
     * @return Field
     * @throws NoSuchFieldException Get field exception
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
     * Get static field，see {@link #getField}
     *
     * @param fieldName field name
     * @return Field
     * @throws NoSuchFieldException Get field exception
     */
    private Field getStaticField(String fieldName) throws NoSuchFieldException {
        final Field field = getField(fieldName);
        if (Modifier.isStatic(field.getModifiers())) {
            return field;
        }
        throw new NoSuchFieldException();
    }

    /**
     * Get member field，see {@link #getField}
     *
     * @param fieldName field name
     * @return Field
     * @throws NoSuchFieldException Get field exception
     */
    private Field getMemberField(String fieldName) throws NoSuchFieldException {
        final Field field = getField(fieldName);
        if (Modifier.isStatic(field.getModifiers())) {
            throw new NoSuchFieldException();
        }
        return field;
    }

    /**
     * Set the rew static field value
     *
     * @param fieldName field name
     * @param value value
     * @throws NoSuchFieldException The field could not be found
     * @throws IllegalAccessException Field access failure
     */
    public void setRawStaticFieldValue(String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        getStaticField(fieldName).set(null, value);
    }

    /**
     * Set additional static field value
     *
     * @param fieldName field name
     * @param value field value
     * @deprecated Deprecated
     */
    @Deprecated
    public void setExtStaticFieldValue(String fieldName, Object value) {
        if (extStaticFields == null) {
            extStaticFields = new HashMap<>();
        }
        extStaticFields.put(fieldName, value);
    }

    /**
     * Get additional static field value
     *
     * @param fieldName field name
     * @return field value
     * @deprecated Deprecated
     */
    @Deprecated
    public Object getExtStaticFieldValue(String fieldName) {
        return extStaticFields == null ? null : extStaticFields.get(fieldName);
    }

    /**
     * Set additional member field value
     *
     * @param fieldName field name
     * @param value field value
     * @deprecated Deprecated
     */
    @Deprecated
    public void setExtMemberFieldValue(String fieldName, Object value) {
        if (extMemberFields == null) {
            extMemberFields = new HashMap<>();
        }
        extMemberFields.put(fieldName, value);
    }

    /**
     * Get additional member field value
     *
     * @param fieldName field name
     * @return field value
     * @deprecated Deprecated
     */
    @Deprecated
    public Object getExtMemberFieldValue(String fieldName) {
        return extMemberFields == null ? null : extMemberFields.get(fieldName);
    }

    /**
     * Set static field value. Write additional static field sets if the raw static field does not exist
     *
     * @param fieldName field name
     * @param value value
     */
    public void setStaticFieldValue(String fieldName, Object value) {
        try {
            setRawStaticFieldValue(fieldName, value);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            setExtStaticFieldValue(fieldName, value);
        }
    }

    /**
     * Get raw static field value
     *
     * @param fieldName field name
     * @return field value
     * @throws NoSuchFieldException The field could not be found
     * @throws IllegalAccessException field access failure
     */
    public Object getRawStaticFieldValue(String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        return getStaticField(fieldName).get(null);
    }

    /**
     * Get the static field value. If raw static field does not exist, it is obtained from the additional static field
     *
     * @param fieldName field name
     * @return field value
     */
    public Object getStaticFieldValue(String fieldName) {
        try {
            return getRawStaticFieldValue(fieldName);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return getExtStaticFieldValue(fieldName);
        }
    }

    /**
     * Set the raw member field value
     *
     * @param fieldName field name
     * @param value field value
     * @throws NoSuchFieldException The field could not be found
     * @throws IllegalAccessException field access failure
     */
    public void setRawMemberFieldValue(String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        getMemberField(fieldName).set(object, value);
    }

    /**
     * Set member field value. Write additional member field sets if the raw member field does not exist
     *
     * @param fieldName field name
     * @param value field value
     * @throws UnsupportedOperationException operation(null) is not supported
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
     * Get raw member field value
     *
     * @param fieldName field name
     * @return field value
     * @throws NoSuchFieldException The field could not be found
     * @throws IllegalAccessException field access failure
     */
    public Object getRawMemberFieldValue(String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        return getMemberField(fieldName).get(object);
    }

    /**
     * Get the member field value. If raw member field does not exist, it is obtained from the additional member field
     *
     * @param fieldName field name
     * @return field value
     * @throws UnsupportedOperationException operation(null) is not supported
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
     * Set local field value
     *
     * @param fieldName 属性名
     * @param value 属性值
     */
    public void setLocalFieldValue(String fieldName, Object value) {
        if (localFields == null) {
            localFields = new HashMap<>();
        }
        localFields.put(fieldName, value);
    }

    /**
     * Get local field value
     *
     * @param fieldName field name
     * @return field value
     */
    public Object getLocalFieldValue(String fieldName) {
        return localFields == null ? null : localFields.get(fieldName);
    }

    /**
     * Change arguments. Note: This method does not do an entry check and requires the user to ensure that the number
     * and type of input parameters are correct
     *
     * @param fixedArguments input arguments
     * @return ExecuteContext
     */
    public ExecuteContext changeArgs(Object[] fixedArguments) {
        this.arguments = fixedArguments;
        return this;
    }

    /**
     * Skip the main execution of method and set the final method result. Note that you cannot skip when enhancing
     * constructors
     *
     * @param fixedResult fixed result
     * @return ExecuteContext
     * @throws UnsupportedOperationException operation(null) is not supported
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
     * Modify the result. Note that this method does not verify the type of the result and requires the user's own
     * judgment
     *
     * @param fixedResult fixed result
     * @return ExecuteContext
     * @throws UnsupportedOperationException operation(null) is not supported
     */
    public ExecuteContext changeResult(Object fixedResult) {
        if (method == null) {
            throw new UnsupportedOperationException("Change result method is not support when enhancing constructor. ");
        }
        this.result = fixedResult;
        return this;
    }

    /**
     * Modify the exception thrown by the method. Note that the method does not verify the type of the result and needs
     * to be judged by the user
     *
     * @param fixedThrowable fixed throwable. When modified to null, the method does not throw a throwable
     * @return ExecuteContext
     * @throws UnsupportedOperationException operation(null) is not supported
     */
    public ExecuteContext changeThrowable(Throwable fixedThrowable) {
        if (method == null) {
            throw new UnsupportedOperationException("Change throwable method is not support when enhancing "
                    + "constructor. ");
        }
        this.throwable = fixedThrowable;
        this.isChangeThrowable = true;
        return this;
    }

    /**
     * Throw an exception to the host instance
     *
     * @param throwableOut An exception thrown to the host instance
     * @return ExecuteContext
     */
    public ExecuteContext setThrowableOut(Throwable throwableOut) {
        this.throwableOut = throwableOut;
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

package com.huawei.apm.premain.enhance.enhancer;

import com.huawei.apm.bootstrap.lubanops.log.LogFactory;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

/**
 * 处理成员变量
 */
public class MemberFieldsHandler {
    private final List<String> fields;

    public MemberFieldsHandler(List<String> fields) {
        this.fields = fields;
    }

    @RuntimeType
    public Object intercept(@This Object obj) {
        if (fields == null || fields.isEmpty()) {
            return new Object[0];
        }
        int size = fields.size();
        Object[] convertedFields = new Object[size];
        try {
            for (int i = 0; i < size; i++) {
                final Field declaredField = obj.getClass().getDeclaredField(fields.get(i));
                AccessController.doPrivileged(new FieldAccessibleAction(declaredField));
                convertedFields[i] = declaredField.get(obj);
            }
        } catch (Exception e) {
            LogFactory.getLogger().warning(String.format("invoke method getLopsFileds failed when convert the member fields! reason:[%s]", e.getMessage()));
        }
        return convertedFields;
    }

    /**
     * 设置字段访问
     */
    static class FieldAccessibleAction implements PrivilegedAction<Object> {
        private final Field field;

        FieldAccessibleAction(Field field) {
            this.field = field;
        }

        @Override
        public Object run() {
            field.setAccessible(true);
            return null;
        }
    }
}

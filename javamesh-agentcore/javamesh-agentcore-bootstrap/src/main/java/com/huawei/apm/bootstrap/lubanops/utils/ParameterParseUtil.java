package com.huawei.apm.bootstrap.lubanops.utils;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.huawei.apm.bootstrap.lubanops.api.APIService;
import com.huawei.apm.bootstrap.lubanops.config.Stats;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;

/**
 * 参数解析工具类
 */
public class ParameterParseUtil {

    private ParameterParseUtil() {
    }

    public static String getString(Map<String, String> parameters, String key) {
        return parameters != null ? parameters.get(key) : null;
    }

    public static Boolean getBoolean(Map<String, String> parameters, String key) {
        String value = getString(parameters, key);
        if (value != null) {
            try {
                return Boolean.valueOf(value);
            } catch (NumberFormatException e) {
                LogFactory.getLogger().warning("Failed to parse parameter " + key + "=" + value);
                return Boolean.FALSE;
            }
        }
        return Boolean.FALSE;
    }

    public static Integer getInteger(Map<String, String> parameters, String key) {
        String value = getString(parameters, key);
        if (value != null) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                LogFactory.getLogger().warning("Failed to parse parameter " + key + "=" + value);
                return null;
            }
        }
        return null;
    }

    public static Long getLong(Map<String, String> parameters, String key) {
        String value = getString(parameters, key);
        if (value != null) {
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException e) {
                LogFactory.getLogger().warning("Failed to parse parameter " + key + "=" + value);
                return null;
            }
        }
        return null;
    }

    public static Double getDouble(Map<String, String> parameters, String key) {
        String value = getString(parameters, key);
        if (value != null) {
            try {
                return Double.valueOf(value);
            } catch (NumberFormatException e) {
                LogFactory.getLogger().warning("Failed to parse parameter " + key + "=" + value);
                return null;
            }
        }
        return null;
    }

    public static <V> V getParameterValue(Map<String, String> parameters, String key, Class<V> classObj,
            Class listType) {
        try {
            if (parameters == null) {
                return null;
            }
            String value = parameters.get(key);
            if (value != null) {
                if (Integer.class.equals(classObj)) {
                    return (V) Integer.valueOf(value);
                } else if (String.class.equals(classObj)) {
                    return (V) value;
                } else if (Stats.class.equals(classObj)) {
                    return (V) Stats.parseValue(value);
                } else if (Boolean.class.equals(classObj)) {
                    return (V) Boolean.valueOf(value);
                } else if (String[].class.equals(classObj)) {
                    List<String> list = APIService.getJsonApi().parseList(value, String.class);
                    return (V) list.toArray(new String[list.size()]);
                } else if (int[].class.equals(classObj)) {
                    return (V) APIService.getJsonApi().parseIntArray(value);
                } else if ("java.util.List".equals(classObj.getName()) && listType != null) {
                    return (V) APIService.getJsonApi().parseList(value, listType);
                } else {
                    return (V) APIService.getJsonApi().parseObject(value, classObj);
                }
            }

        } catch (Exception e) {
            LogFactory.getLogger().log(Level.SEVERE, "parseUrlParameters error", e);
        }
        return null;
    }

    public static <V> V getParameterValue(Map<String, String> parameters, String key, Class<V> classObj) {
        return getParameterValue(parameters, key, classObj, null);
    }
}

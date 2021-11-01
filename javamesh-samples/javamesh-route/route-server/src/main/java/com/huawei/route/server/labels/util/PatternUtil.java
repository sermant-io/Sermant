package com.huawei.route.server.labels.util;

import com.huawei.route.server.labels.constant.LabelConstant;
import com.huawei.route.server.labels.exception.CustomGenericException;

import java.util.regex.Pattern;

import static com.huawei.route.server.labels.constant.LabelConstant.ERROR_CODE_ONE;

/**
 * 正则校验工具类
 *
 * @author pengyuyi
 * @date 2021/8/11
 */
public class PatternUtil {
    public static void checkLabelGroupName(String labelGroupName) {
        if (!Pattern.matches(LabelConstant.PATTERN_WITH_CHINESE, labelGroupName)) {
            throw new CustomGenericException(ERROR_CODE_ONE, "标签组名只支持中文、字母、数字、中划线和下划线");
        }
    }

    public static void checkLabelGroupNameAndLabelName(String labelGroupName, String labelName) {
        checkLabelGroupName(labelGroupName);
        if (!Pattern.matches(LabelConstant.PATTERN_WITHOUT_CHINESE, labelName)) {
            throw new CustomGenericException(ERROR_CODE_ONE, "标签名只支持字母、数字、中划线和下划线");
        }
    }

    public static void checkConfigName(String configName) {
        if (!Pattern.matches(LabelConstant.PATTERN_WITHOUT_CHINESE, configName)) {
            throw new CustomGenericException(ERROR_CODE_ONE, "配置名只支持字母、数字、中划线和下划线");
        }
    }
}

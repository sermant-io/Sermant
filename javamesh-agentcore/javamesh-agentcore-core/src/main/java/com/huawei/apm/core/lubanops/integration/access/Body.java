package com.huawei.apm.core.lubanops.integration.access;

import com.huawei.apm.core.lubanops.integration.Constants;
import com.huawei.apm.core.lubanops.integration.utils.JSON;

/**
 * @author
 * @since 2020/5/7
 **/
public abstract class Body {
    /**
     * 转成json的二进制
     * @return
     */
    public byte[] toBytes() {

        String s = JSON.toJSONString(this);

        return s.getBytes(Constants.DEFAULT_CHARSET);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}

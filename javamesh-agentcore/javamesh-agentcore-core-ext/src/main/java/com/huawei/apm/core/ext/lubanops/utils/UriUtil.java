package com.huawei.apm.core.ext.lubanops.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.huawei.apm.core.ext.lubanops.Constants;
import com.huawei.apm.core.ext.lubanops.access.HMacAlgorithm;
import com.huawei.apm.core.ext.lubanops.access.HMacSignatureUtil;

/**
 * @author
 * @since 2020/5/16
 **/
public class UriUtil {
    private static String encodeString(String s) {
        try {
            return URLEncoder.encode(s, Constants.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    /**
     * 构造socket连接的uri
     *
     * @param address    websocket的地址
     * @param ak         access key
     * @param sk         secret key
     * @param instanceId 实例ID
     * @return
     */
    public static String buildUri(String address, String ak, String sk, long instanceId) {
        String ts = String.valueOf(System.currentTimeMillis() / 1000);
        String signature = HMacSignatureUtil.getHmacSign(ts, sk, HMacAlgorithm.HmacSHA256);
        StringBuilder sb = new StringBuilder();
        sb.append(address);
        sb.append("/access-server/");
        sb.append(encodeString(ak));
        sb.append("/").append(ts).append("/");
        sb.append(signature).append("/").append(instanceId);
        return sb.toString();
    }
}

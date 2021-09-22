package com.lubanops.apm.integration.transport.http;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.lubanops.apm.bootstrap.config.AgentConfigManager;
import com.lubanops.apm.integration.utils.BinaryUtils;

/**
 * @author
 * @date 2020/8/7 15:11
 */
public class HttpSigner extends AbstractHttpSinger {

    public static final String TS = "apm2ts";

    public static final String AK = "apm2ak";

    public static final String SIG = "apm2sig";

    @Override
    public void sign(Request request) throws InvalidKeyException, NoSuchAlgorithmException {
        HttpRequest httpRequest = (HttpRequest) request;
        String singerDate = this.getHeader(httpRequest, TS);
        if (singerDate == null) {
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMATTER);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            singerDate = sdf.format(new Date());
            httpRequest.addHeader(TS, singerDate);
        }
        httpRequest.addHeader(AK, AgentConfigManager.getMasterAuthAk());
        byte[] signingKey = this.deriveSigningKey(AgentConfigManager.getMasterAuthSk());
        byte[] signature = this.computeSignature(singerDate, signingKey);
        httpRequest.setSignature(BinaryUtils.toHex(signature));

    }

}

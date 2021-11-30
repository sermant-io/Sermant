package com.huawei.javamesh.core.lubanops.integration.transport.http;

/**
 * @author
 * @date 2020/8/7 17:49
 */
public abstract class AbstractHttpSinger extends AbstractSigner {

    String getHeader(HttpRequest request, String header) {
        if (header == null) {
            return null;
        } else {
            return request.getHeaders().get(header);
        }
    }

}

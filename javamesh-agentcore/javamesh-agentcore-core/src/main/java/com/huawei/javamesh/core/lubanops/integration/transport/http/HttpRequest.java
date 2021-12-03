/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.javamesh.core.lubanops.integration.transport.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;

import com.huawei.javamesh.core.lubanops.integration.enums.HttpMethod;
import com.huawei.javamesh.core.lubanops.integration.utils.HttpUtils;

/**
 * @author
 * @date 2020/8/7 15:05
 */
public class HttpRequest implements Request {
    private String method = null;

    private String url = null;

    private String body = null;

    private String fragment = null;

    private Map<String, String> headers = new Hashtable();

    private Map<String, List<String>> queryString = new Hashtable();

    @Override
    public void setSignature(String signature) {
        this.addHeader(HttpSigner.SIG, signature);
    }

    @Override
    public HttpRequestBase generate() throws UnsupportedEncodingException {
        HttpRequestBase httpRequest;
        StringEntity entity;
        if (HttpMethod.POST.name().equals(method)) {
            HttpPost postMethod = new HttpPost(url);
            if (this.getBody() != null) {
                entity = new StringEntity(this.getBody(), Charset.forName("UTF-8"));
                postMethod.setEntity(entity);
            }

            httpRequest = postMethod;
        } else if (HttpMethod.PUT.name().equals(method)) {
            HttpPut putMethod = new HttpPut(url);
            httpRequest = putMethod;
            if (this.getBody() != null) {
                entity = new StringEntity(this.getBody(), Charset.forName("UTF-8"));
                putMethod.setEntity(entity);
            }
        } else if (HttpMethod.PATCH.name().equals(method)) {
            HttpPatch patchMethod = new HttpPatch(url);
            httpRequest = patchMethod;
            if (this.getBody() != null) {
                entity = new StringEntity(this.getBody(), Charset.forName("UTF-8"));
                patchMethod.setEntity(entity);
            }
        } else if (HttpMethod.GET.name().equals(method)) {
            httpRequest = new HttpGet(url);
        } else if (HttpMethod.DELETE.name().equals(method)) {
            httpRequest = new HttpDelete(url);
        } else if (HttpMethod.OPTIONS.name().equals(method)) {
            httpRequest = new HttpOptions(url);
        } else {
            if (!HttpMethod.HEAD.name().equals(method)) {
                throw new RuntimeException("Unknown HTTP method name: " + method);
            }

            httpRequest = new HttpHead(url);
        }
        Map<String, String> requestHeaders = this.getHeaders();

        for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase("Content-Length".toString())) {
                String value = requestHeaders.get(entry.getKey());
                httpRequest.addHeader(entry.getKey(), new String(value.getBytes("UTF-8"), "ISO-8859-1"));
            }
        }

        return httpRequest;
    }

    public HttpMethod getMethod() {
        return HttpMethod.valueOf(this.method.toUpperCase());
    }

    public void setMethod(String method) throws Exception {
        if (null == method) {
            throw new Exception("method can not be empty");
        } else if (!method.equalsIgnoreCase("post") && !method.equalsIgnoreCase("put") && !method.equalsIgnoreCase(
                "patch") && !method.equalsIgnoreCase("delete") && !method.equalsIgnoreCase("get")
                && !method.equalsIgnoreCase("options") && !method.equalsIgnoreCase("head")) {
            throw new Exception("unsupported method");
        } else {
            this.method = method;
        }
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public String getUrl() {
        StringBuilder sBuilder = new StringBuilder(this.url);
        sBuilder.append("?");
        if (this.queryString.size() > 0) {
            int loop = 0;
            Iterator iterator = this.queryString.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<String, List<String>> entry = (Entry) iterator.next();

                for (Iterator paraIterator = ((List) entry.getValue()).iterator(); paraIterator.hasNext(); ++loop) {
                    String value = (String) paraIterator.next();

                    if (loop > 0) {
                        sBuilder.append("&");
                    }
                    sBuilder.append(HttpUtils.urlEncode(entry.getKey(), false));
                    sBuilder.append("=");
                    sBuilder.append(HttpUtils.urlEncode(value, false));
                }
            }
        }

        if (this.fragment != null) {
            sBuilder.append("#");
            sBuilder.append(this.fragment);
        }

        return sBuilder.toString();
    }

    public void setUrl(String url) throws Exception {
        if (null != url && !url.trim().isEmpty()) {
            int i = url.indexOf(35);
            if (i >= 0) {
                url = url.substring(0, i);
            }

            i = url.indexOf(63);
            if (i >= 0) {
                String query = url.substring(i + 1, url.length());
                String[] queryArr = query.split("&");
                int queryLen = queryArr.length;

                for (int j = 0; j < queryLen; ++j) {
                    String item = queryArr[j];
                    String[] spl = item.split("=", 2);
                    String key = spl[0];
                    String value = "";
                    if (spl.length > 1) {
                        value = spl[1];
                    }

                    if (!key.trim().isEmpty()) {
                        key = URLDecoder.decode(key, "UTF-8");
                        value = URLDecoder.decode(value, "UTF-8");
                        this.addQueryStringParam(key, value);
                    }
                }

                url = url.substring(0, i);
            }

            this.url = url;
        } else {
            throw new Exception("url can not be empty");
        }
    }

    public String getPath() {
        String url = this.url;
        int i = url.indexOf("://");
        if (i >= 0) {
            url = url.substring(i + 3);
        }

        i = url.indexOf(47);
        return i >= 0 ? url.substring(i) : "/";
    }

    public String getHost() {
        String url = this.url;
        int i = url.indexOf("://");
        if (i >= 0) {
            url = url.substring(i + 3);
        }

        i = url.indexOf(47);
        if (i >= 0) {
            url = url.substring(0, i);
        }

        return url;
    }

    public void addQueryStringParam(String name, String value) throws UnsupportedEncodingException {
        List<String> paramList = (List) this.queryString.get(name);
        if (paramList == null) {
            paramList = new ArrayList();
            this.queryString.put(name, paramList);
        }

        ((List) paramList).add(value);
    }

    public Map<String, List<String>> getQueryStringParams() {
        return this.queryString;
    }

    public String getFragment() {
        return this.fragment;
    }

    public void setFragment(String fragment) throws Exception {
        if (null != fragment && !fragment.trim().isEmpty()) {
            this.fragment = URLEncoder.encode(fragment, "UTF-8");
        } else {
            throw new Exception("fragment can not be empty");
        }
    }

    public void addHeader(String name, String value) {
        if (null != name && !name.trim().isEmpty()) {
            this.headers.put(name, value);
        }
    }

}

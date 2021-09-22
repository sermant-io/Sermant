package com.lubanops.apm.bootstrap;

import java.util.List;
import java.util.Set;

public class TransformerMethod {

    private String method;

    private List<String> params;

    private Set<String> excludeMethods;

    private boolean isConstructor = false;

    private boolean interceptorGetAndSet = false;

    private String interceptor;

    public TransformerMethod(String method, List<String> params, String interceptor) {
        this.interceptor = interceptor;
        this.method = method;
        this.params = params;
    }

    public TransformerMethod(String method, List<String> params, String interceptor, boolean isConstructor) {
        this.interceptor = interceptor;
        this.method = method;
        this.params = params;
        this.setConstructor(isConstructor);
    }

    public TransformerMethod(String method, String interceptor, Set<String> excludeMethods,
            boolean interceptorGetAndSet) {
        this.interceptor = interceptor;
        this.method = method;
        this.excludeMethods = excludeMethods;
        this.interceptorGetAndSet = interceptorGetAndSet;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public String getInterceptor() {
        return interceptor;
    }

    public void setInterceptor(String interceptor) {
        this.interceptor = interceptor;
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public void setConstructor(boolean isConstructor) {
        this.isConstructor = isConstructor;
    }

    public Set<String> getExcludeMethods() {
        return excludeMethods;
    }

    public void setExcludeMethods(Set<String> excludeMethods) {
        this.excludeMethods = excludeMethods;
    }

    public boolean isInterceptorGetAndSet() {
        return interceptorGetAndSet;
    }

    public void setInterceptorGetAndSet(boolean interceptorGetAndSet) {
        this.interceptorGetAndSet = interceptorGetAndSet;
    }

}

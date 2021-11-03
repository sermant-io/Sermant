package com.huawei.apm.core.agent.common;

/**
 * 实例和静态方法拦截器前置方法执行结果承载类
 */
public class BeforeResult {

    private boolean isContinue = true;

    private Object result;

    public void setResult(Object result) {
        this.result = result;
        isContinue = false;
    }

    public boolean isContinue() {
        return isContinue;
    }

    public Object getResult() {
        return result;
    }
}

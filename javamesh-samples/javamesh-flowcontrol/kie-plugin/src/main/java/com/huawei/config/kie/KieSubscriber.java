/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.config.kie;

/**
 * 增加是否为长请求判断
 *
 * @author zhouss
 * @since 2021-11-18
 */
public class KieSubscriber {
    /**
     * 最大等待时间
     * 50S
     */
    private static final int MAX_WAIT = 50;

    private Boolean isLongConnectionRequest;

    private final KieRequest kieRequest;

    public KieSubscriber(KieRequest kieRequest) {
        this.kieRequest = kieRequest;
    }

    /**
     * 是否为长请求
     *
     * @return boolean
     */
    public boolean isLongConnectionRequest() {
        String wait = kieRequest.getWait();
        if (this.isLongConnectionRequest != null) {
            return this.isLongConnectionRequest;
        }
        if (wait == null || wait.trim().length() == 0) {
            this.isLongConnectionRequest = false;
            return false;
        }
        try {
            final int parseWait = Integer.parseInt(wait);
            this.isLongConnectionRequest = parseWait >= 1;
            if (parseWait > MAX_WAIT) {
                kieRequest.setWait(String.valueOf(MAX_WAIT));
            }
        } catch (Exception ex) {
            this.isLongConnectionRequest = false;
        }
        return this.isLongConnectionRequest;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        KieSubscriber that = (KieSubscriber) obj;

        if (!isLongConnectionRequest.equals(that.isLongConnectionRequest)) {
            return false;
        }
        return kieRequest != null ? kieRequest.equals(that.kieRequest) : that.kieRequest == null;
    }

    @Override
    public int hashCode() {
        int result = ((isLongConnectionRequest == null || !isLongConnectionRequest)  ? 1 : 0);
        result = 31 * result + (kieRequest != null ? kieRequest.hashCode() : 0);
        return result;
    }

    public KieRequest getKieRequest() {
        return kieRequest;
    }
}

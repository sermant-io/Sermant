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
    private Boolean isKeeperRequest;

    private final KieRequest kieRequest;

    public KieSubscriber(KieRequest kieRequest) {
        this.kieRequest = kieRequest;
    }

    /**
     * 是否为长请求
     *
     * @return boolean
     */
    public boolean isKeeperRequest() {
        String wait = kieRequest.getWait();
        if (this.isKeeperRequest != null) {
            return this.isKeeperRequest;
        }
        if (wait == null || wait.trim().length() == 0) {
            this.isKeeperRequest = false;
            return false;
        }
        try {
            final int parseWait = Integer.parseInt(wait);
            this.isKeeperRequest = parseWait >= 1;
        } catch (Exception ex) {
            this.isKeeperRequest = false;
        }
        return this.isKeeperRequest;
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

        if (!isKeeperRequest.equals(that.isKeeperRequest)) {
            return false;
        }
        return kieRequest != null ? kieRequest.equals(that.kieRequest) : that.kieRequest == null;
    }

    @Override
    public int hashCode() {
        int result = ((isKeeperRequest == null || !isKeeperRequest )  ? 1 : 0);
        result = 31 * result + (kieRequest != null ? kieRequest.hashCode() : 0);
        return result;
    }

    public KieRequest getKieRequest() {
        return kieRequest;
    }
}

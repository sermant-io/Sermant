/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.core.service.xds.entity;

/**
 * Certificate from istiod
 *
 * @author lilai
 * @since 2025-05-26
 */
public class IstiodCertificate {
    private final String privateKey;

    private final String certificateChain;

    private final String password;

    private final long createTime;

    private final long expireTime;

    /**
     * Constructor
     *
     * @param privateKey private key
     * @param certificateChain certificateChain
     * @param createTime creat time
     * @param expireTime expire time
     */
    public IstiodCertificate(String privateKey, String certificateChain, long createTime, long expireTime) {
        this.privateKey = privateKey;
        this.certificateChain = certificateChain;
        this.createTime = createTime;
        this.expireTime = expireTime;
        this.password = null;
    }

    /**
     * Constructor
     *
     * @param privateKey private key
     * @param certificateChain certificateChain
     * @param password password
     * @param createTime creat time
     * @param expireTime expire time
     */
    public IstiodCertificate(String privateKey, String certificateChain, String password, long createTime,
            long expireTime) {
        this.privateKey = privateKey;
        this.certificateChain = certificateChain;
        this.password = password;
        this.createTime = createTime;
        this.expireTime = expireTime;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getCertificateChain() {
        return certificateChain;
    }

    public long getCreateTime() {
        return createTime;
    }

    public boolean isExpire() {
        return System.currentTimeMillis() < expireTime;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public String getPassword() {
        return password;
    }
}

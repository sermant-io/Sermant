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

package io.sermant.xds.traffic.management.cert;

import java.security.KeyStore;

/**
 * CertStore Cache
 *
 * @author lilai
 * @since 2025-06-04
 */
public enum CertStoreCache {
    /**
     * Singleton instance
     */
    INSTANCE;

    private KeyStore sermantKeyStore = null;

    private KeyStore sermantTrustStore = null;

    /**
     * update KeyStore
     *
     * @param keyStore new KeyStore
     */
    public void updateKeyStore(KeyStore keyStore) {
        sermantKeyStore = keyStore;
    }

    /**
     * update TrustStore
     *
     * @param trustStore new TrustStore
     */
    public void updateTrustStore(KeyStore trustStore) {
        sermantTrustStore = trustStore;
    }

    /**
     * get KeyStore
     *
     * @return KeyStore
     */
    public KeyStore getSermantKeyStore() {
        return sermantKeyStore;
    }

    /**
     * get TrustStore
     *
     * @return TrustStore
     */
    public KeyStore getSermantTrustStore() {
        return sermantTrustStore;
    }
}

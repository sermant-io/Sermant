/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.implement.service.dynamicconfig.kie.client;

import io.sermant.implement.service.dynamicconfig.kie.selector.url.UrlSelector;

import java.util.Arrays;
import java.util.List;

/**
 * Client url manager
 *
 * @author zhouss
 * @since 2021-11-17
 */
public class ClientUrlManager {
    /**
     * kie http protocol
     */
    private static final String KIE_PROTOCOL = "http://";

    /**
     * kie https protocol
     */
    private static final String KIE_PROTOCOL_SSL = "https://";

    private final UrlSelector urlSelector = new UrlSelector();

    private List<String> urls;

    /**
     * Constructor.
     *
     * @param serverAddress server address
     */
    public ClientUrlManager(String serverAddress) {
        resolveUrls(serverAddress);
    }

    /**
     * Client request address
     *
     * @return url
     */
    public String getUrl() {
        return urlSelector.select(urls);
    }

    /**
     * Parse url, multiple urls are separated by commas by default
     *
     * @param serverAddress server address
     */
    private void resolveUrls(String serverAddress) {
        if (serverAddress == null || serverAddress.trim().length() == 0) {
            return;
        }
        final String[] addressArr = serverAddress.split(",");
        for (int i = 0; i < addressArr.length; i++) {
            addressArr[i] = addressArr[i].trim();
            if (addressArr[i].startsWith(KIE_PROTOCOL) || addressArr[i].startsWith(KIE_PROTOCOL_SSL)) {
                continue;
            }
            addressArr[i] = KIE_PROTOCOL + addressArr[i];
        }
        urls = Arrays.asList(addressArr);
    }
}

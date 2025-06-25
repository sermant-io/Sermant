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

package io.sermant.core.service.xds.entity.match;

import io.sermant.core.utils.NetworkUtils;
import io.sermant.core.utils.StringUtils;

import java.util.Objects;

/**
 * IP address matcher, used to match IP addresses and CIDR prefixes
 *
 * @since 2025-06-17
 */
public class IpMatcher implements XdsAuthorizationMatcher<String> {
    private static final int BINARY_IP_LENGTH = 32;

    /**
     * CIDR prefix length
     */
    private final int prefixLength;

    /**
     * Binary format IP address string
     */
    private final String binaryIpString;

    /**
     * Constructor
     *
     * @param prefixLength CIDR prefix length
     * @param ipAddress IP address
     * @throws IllegalArgumentException if the prefix length is not in the range [0, 32]
     */
    public IpMatcher(int prefixLength, String ipAddress) {
        if (prefixLength < 0 || prefixLength > BINARY_IP_LENGTH) {
            throw new IllegalArgumentException("Prefix length must be between 0 and 32");
        }
        this.prefixLength = prefixLength;
        this.binaryIpString = NetworkUtils.convertIpToBinaryString(ipAddress);
    }

    @Override
    public boolean match(String ipToMatch) {
        if (StringUtils.isEmpty(binaryIpString) || StringUtils.isEmpty(ipToMatch)) {
            return false;
        }

        String binaryIpToMatch = NetworkUtils.convertIpToBinaryString(ipToMatch);
        if (StringUtils.isEmpty(binaryIpToMatch)) {
            return false;
        }

        if (prefixLength <= 0) {
            return binaryIpString.equals(binaryIpToMatch);
        }

        int compareLength = Math.min(prefixLength, Math.min(binaryIpString.length(), binaryIpToMatch.length()));
        return binaryIpString.substring(0, compareLength)
                .equals(binaryIpToMatch.substring(0, compareLength));
    }

    public int getPrefixLength() {
        return prefixLength;
    }

    public String getBinaryIpString() {
        return binaryIpString;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        IpMatcher ipMatcher = (IpMatcher) object;
        return prefixLength == ipMatcher.prefixLength && Objects.equals(binaryIpString, ipMatcher.binaryIpString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefixLength, binaryIpString);
    }
}

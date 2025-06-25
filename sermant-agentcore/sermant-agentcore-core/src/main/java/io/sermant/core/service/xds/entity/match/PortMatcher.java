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

import io.sermant.core.common.LoggerFactory;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Port matcher implementation for XDS authorization, used to match port numbers
 *
 * @since 2025-06-17
 */
public class PortMatcher implements XdsAuthorizationMatcher<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int MIN_PORT = 0;

    private static final int MAX_PORT = 65535;

    private final int port;

    /**
     * Constructs a PortMatcher with specified port number
     *
     * @param port the port number to match, must be between 0-65535
     * @throws IllegalArgumentException if port is out of valid range
     */
    public PortMatcher(int port) {
        if (port < MIN_PORT || port > MAX_PORT) {
            LOGGER.warning("Invalid port number: " + port);
            throw new IllegalArgumentException("Port must be between " + MIN_PORT + " and " + MAX_PORT);
        }
        this.port = port;
    }

    @Override
    public boolean match(Integer portToMatch) {
        if (portToMatch == null) {
            LOGGER.warning("Input port to match is null");
            return false;
        }
        return portToMatch.equals(port);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        PortMatcher that = (PortMatcher) object;
        return port == that.port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(port);
    }
}

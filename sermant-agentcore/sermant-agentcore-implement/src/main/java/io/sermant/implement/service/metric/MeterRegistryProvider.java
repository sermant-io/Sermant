/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.implement.service.metric;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Interface defining the core components for monitoring data collection. It provides access and configuration
 * capabilities for the monitoring data registry, used for collecting and exposing metrics.
 *
 * @author zwmagic
 * @since 2024-08-19
 */
public interface MeterRegistryProvider {
    /**
     * Gets the type of the monitoring registry.
     *
     * @return The type of the monitoring data registry
     */
    String getType();

    /**
     * Gets an instance of the monitoring data registry.
     *
     * @return A MeterRegistry instance
     */
    MeterRegistry getRegistry();

    /**
     * Gets the configuration information for scraping monitoring data.
     *
     * @return The configuration information for scraping monitoring data
     */
    String getScrape();
}

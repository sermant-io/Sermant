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

package io.sermant.core.ext.otel;

/**
 * OpenTelemetry Constant
 *
 * @author lilai
 * @since 2024-12-17
 */
public class OtelConstant {
    /**
     * Name of OpenTelemetry agent for installation in Sermant
     */
    public static final String OTEL = "OTEL";

    /**
     * Key of the excluded classloaders for OpenTelemetry agent
     */
    public static final String OTEL_JAVAAGENT_EXCLUDE_CLASS_LOADERS = "otel.javaagent.exclude-class-loaders";

    /**
     * Key of the excluded classes for OpenTelemetry agent
     */
    public static final String OTEL_JAVAAGENT_EXCLUDE_CLASSES = "otel.javaagent.exclude-classes";

    /**
     * Classes ignored in OpenTelemetry agent to avoid conflicts
     */
    public static final String IO_SERMANT_PREFIX = "io.sermant.*";

    /**
     * Entrance class of OpenTelemetry agent
     */
    public static final String OTEL_AGENT_CLASS = "io.opentelemetry.javaagent.OpenTelemetryAgent";

    private OtelConstant() {
    }
}

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

package io.sermant.mq.grayscale.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * grayscale entry
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class Grayscale {
    private Map<String, List<String>> environmentMatch = new HashMap<>();

    private Map<String, List<String>> trafficMatch = new HashMap<>();

    public Map<String, List<String>> getEnvironmentMatch() {
        return environmentMatch;
    }

    public void setEnvironmentMatch(Map<String, List<String>> environmentMatch) {
        this.environmentMatch = environmentMatch;
    }

    public Map<String, List<String>> getTrafficMatch() {
        return trafficMatch;
    }

    public void setTrafficMatch(Map<String, List<String>> trafficMatch) {
        this.trafficMatch = trafficMatch;
    }
}

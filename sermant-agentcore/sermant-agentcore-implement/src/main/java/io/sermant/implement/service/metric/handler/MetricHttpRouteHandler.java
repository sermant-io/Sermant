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

package io.sermant.implement.service.metric.handler;

import io.prometheus.client.exporter.common.TextFormat;
import io.sermant.core.service.httpserver.annotation.HttpRouteMapping;
import io.sermant.core.service.httpserver.api.HttpMethod;
import io.sermant.core.service.httpserver.api.HttpRequest;
import io.sermant.core.service.httpserver.api.HttpResponse;
import io.sermant.core.service.httpserver.api.HttpRouteHandler;
import io.sermant.implement.service.metric.MeterMetricServiceImpl;

/**
 * provide http metric
 *
 * @author zwmagic
 * @since 2024-08-19
 */
@HttpRouteMapping(path = "/metrics", method = HttpMethod.GET)
public class MetricHttpRouteHandler implements HttpRouteHandler {
    private static final int SUCCESS_CODE = 200;

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        String scrape = MeterMetricServiceImpl.getScrape();
        response.setStatus(SUCCESS_CODE)
                .setContentType(TextFormat.CONTENT_TYPE_004)
                .writeBody(scrape);
    }
}

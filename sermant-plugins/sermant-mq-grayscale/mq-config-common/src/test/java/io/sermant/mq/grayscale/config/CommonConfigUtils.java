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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * common config utils
 *
 * @author chengyouling
 * @since 2024-09-18
 **/
public class CommonConfigUtils {
    public static GrayTagItem getGrayTagItem() {
        GrayTagItem item = new GrayTagItem();
        item.setConsumerGroupTag("gray");
        Map<String, String> serviceMeta = new HashMap<>();
        serviceMeta.put("x_lane_tag", "gray" );
        item.setServiceMeta(serviceMeta);
        Map<String, String> trafficTag = new HashMap<>();
        trafficTag.put("x_lane_canary", "gray");
        item.setTrafficTag(trafficTag);
        return item;
    }

    public static MqGrayscaleConfig getMqGrayscaleConfig() {
        MqGrayscaleConfig config = new MqGrayscaleConfig();
        config.setGrayscale(getGrayscale(getGrayTagItem()));
        config.setEnabled(true);
        return config;
    }


    public static List<GrayTagItem> getGrayscale(GrayTagItem item) {
        List<GrayTagItem> grayscale = new ArrayList<>();
        grayscale.add(item);
        return grayscale;
    }
}

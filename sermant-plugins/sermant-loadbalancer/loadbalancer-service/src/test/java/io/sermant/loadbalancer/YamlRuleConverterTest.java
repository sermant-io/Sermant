/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.loadbalancer;

import static org.junit.Assert.assertEquals;

import io.sermant.loadbalancer.rule.LoadbalancerRule;
import io.sermant.loadbalancer.service.RuleConverter;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * yamlconversion test
 *
 * @author zhouss
 * @since 2022-08-27
 */
public class YamlRuleConverterTest {
    @Test
    public void test() {
        final RuleConverter yamlRuleConverter = new YamlRuleConverter();
        final Optional<LoadbalancerRule> convert = yamlRuleConverter
                .convert("rule: Random\nserviceName: test", LoadbalancerRule.class);
        Assert.assertTrue(convert.isPresent());
        assertEquals("Random", convert.get().getRule());
        assertEquals("test", convert.get().getServiceName());

        final Optional<Map> foo = yamlRuleConverter.convert(getMatchGroup("foo"), Map.class);
        Assert.assertTrue(foo.isPresent());
        Map<String, Object> map = foo.get();
        final Object matches = map.get("matches");
        Assert.assertTrue(matches instanceof List);
        Assert.assertTrue(((List<?>) matches).size() > 0);
        final Object content = ((List<?>) matches).get(0);
        Assert.assertTrue(content instanceof Map);
        final Object serviceName = ((Map<?, ?>) content).get("serviceName");
        Assert.assertEquals(serviceName, "foo");
    }

    private static String getMatchGroup(String serviceName) {
        return "alias: flowcontrol111\n"
                + "matches:\n"
                + "- apiPath:\n"
                + "    exact: /sc/provider/\n"
                + "  headers: {}\n"
                + "  method:\n"
                + "  - GET\n"
                + "  name: degrade\n"
                + "  showAlert: false\n"
                + "  uniqIndex: c3w7x\n"
                + ((serviceName != null) ? ("  serviceName: " + serviceName + "\n") : "");
    }
}

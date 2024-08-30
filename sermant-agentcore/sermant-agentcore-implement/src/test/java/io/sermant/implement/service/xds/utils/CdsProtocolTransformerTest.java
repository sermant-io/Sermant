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

package io.sermant.implement.service.xds.utils;

import io.envoyproxy.envoy.config.cluster.v3.Cluster;
import io.envoyproxy.envoy.config.cluster.v3.Cluster.CommonLbConfig;
import io.envoyproxy.envoy.config.cluster.v3.Cluster.CommonLbConfig.Builder;
import io.envoyproxy.envoy.config.cluster.v3.Cluster.CommonLbConfig.LocalityWeightedLbConfig;
import io.envoyproxy.envoy.config.cluster.v3.Cluster.LbPolicy;
import io.sermant.core.service.xds.entity.XdsLbPolicy;
import io.sermant.core.service.xds.entity.XdsServiceCluster;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * CdsProtocolTransformerTest
 *
 * @author daizhenyu
 * @since 2024-08-23
 **/
public class CdsProtocolTransformerTest {
    @Test
    public void testGetServiceClusters() {
        List<Cluster> clusters = Arrays.asList(
                null,
                createCluster("outbound|8080||serviceA.default.svc.cluster.local", LbPolicy.RANDOM, true),
                createCluster("outbound|8080|subset1|serviceB.default.svc.cluster.local", LbPolicy.RANDOM, false),
                createCluster("outbound|8080|subset2|serviceB.default.svc.cluster.local", LbPolicy.CLUSTER_PROVIDED,
                        false),
                createCluster("outbound|8080||serviceB.default.svc.cluster.local", LbPolicy.RANDOM, false),
                createCluster("outbound|8080|serviceC.default.svc.cluster.local", LbPolicy.RANDOM, false),
                createCluster(null, null, false)
        );

        Map<String, XdsServiceCluster> result = CdsProtocolTransformer.getServiceClusters(clusters);
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.containsKey("serviceA"));
        Assert.assertTrue(result.containsKey("serviceB"));
        XdsServiceCluster serviceACluster = result.get("serviceA");
        XdsServiceCluster serviceBCluster = result.get("serviceB");
        Assert.assertEquals(1, serviceACluster.getClusterResources().size());
        Assert.assertEquals("outbound|8080||serviceA.default.svc.cluster.local",
                serviceACluster.getBaseClusterName());
        Assert.assertEquals(XdsLbPolicy.RANDOM,
                serviceACluster.getLbPolicyOfCluster("outbound|8080||serviceA.default.svc.cluster.local"));
        Assert.assertEquals(XdsLbPolicy.UNRECOGNIZED,
                serviceACluster.getLbPolicyOfCluster("outbound|8080|aaa|serviceA.default.svc.cluster.local"));
        Assert.assertEquals(XdsLbPolicy.RANDOM,
                serviceACluster.getBaseLbPolicyOfService());
        Assert.assertEquals(true,
                serviceACluster.isClusterLocalityLb("outbound|8080||serviceA.default.svc.cluster.local"));

        Assert.assertEquals(3, serviceBCluster.getClusterResources().size());
        Assert.assertEquals("outbound|8080||serviceA.default.svc.cluster.local",
                result.get("serviceA").getBaseClusterName());
        Assert.assertEquals(XdsLbPolicy.RANDOM,
                serviceBCluster.getLbPolicyOfCluster("outbound|8080||serviceB.default.svc.cluster.local"));
        Assert.assertEquals(XdsLbPolicy.UNRECOGNIZED,
                serviceBCluster.getLbPolicyOfCluster("outbound|8080|subset2|serviceB.default.svc.cluster.local"));
        Assert.assertEquals(XdsLbPolicy.UNRECOGNIZED,
                serviceBCluster.getLbPolicyOfCluster("outbound|8080|subset3|serviceB.default.svc.cluster.local"));
        Assert.assertEquals(XdsLbPolicy.RANDOM,
                serviceBCluster.getBaseLbPolicyOfService());
        Assert.assertEquals(false,
                serviceACluster.isClusterLocalityLb("outbound|8080||serviceB.default.svc.cluster.local"));
    }

    private Cluster createCluster(String name, LbPolicy lbPolicy, boolean isLocal) {
        Cluster.Builder builder = Cluster.newBuilder();
        if (name != null) {
            builder.setName(name);
        }
        if (lbPolicy != null) {
            builder.setLbPolicy(lbPolicy);
        }
        if (isLocal) {
            Builder configBuilder = CommonLbConfig.newBuilder();
            configBuilder.setLocalityWeightedLbConfig(LocalityWeightedLbConfig.newBuilder().build());
            builder.setCommonLbConfig(configBuilder.build());
        }
        return builder.build();
    }
}
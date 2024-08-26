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

package io.sermant.core.service.xds.entity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author daizhenyu
 * @since 2024-08-24
 **/
public class XdsServiceClusterTest {
    private XdsServiceCluster serviceCluster;
    private XdsCluster xdsCluster1;
    private XdsCluster xdsCluster2;

    @Before
    public void setUp() {
        serviceCluster = new XdsServiceCluster();

        xdsCluster1 = new XdsCluster();
        xdsCluster1.setClusterName("outbound|8080||serviceA.default.svc.cluster.local");
        xdsCluster1.setLocalityLb(true);
        xdsCluster1.setLbPolicy(XdsLbPolicy.ROUND_ROBIN);

        xdsCluster2 = new XdsCluster();
        xdsCluster2.setLocalityLb(false);
        xdsCluster2.setLbPolicy(XdsLbPolicy.LEAST_REQUEST);
        xdsCluster1.setClusterName("outbound|8080|subset1|serviceA.default.svc.cluster.local");

        Map<String, XdsCluster> clusters = new HashMap<>();
        clusters.put("outbound|8080||serviceA.default.svc.cluster.local", xdsCluster1);
        clusters.put("outbound|8080|subset1|serviceA.default.svc.cluster.local", xdsCluster2);

        serviceCluster.setClusters(clusters);
        serviceCluster.setBaseClusterName("outbound|8080||serviceA.default.svc.cluster.local");
    }

    @Test
    public void testGetBaseClusterName() {
        Assert.assertEquals("outbound|8080||serviceA.default.svc.cluster.local", serviceCluster.getBaseClusterName());
    }

    @Test
    public void testGetClusterResources() {
        Set<String> clusterResources = serviceCluster.getClusterResources();
        Assert.assertNotNull(clusterResources);
        Assert.assertEquals(2, clusterResources.size());
        Assert.assertTrue(clusterResources.contains("outbound|8080||serviceA.default.svc.cluster.local"));
        Assert.assertTrue(clusterResources.contains("outbound|8080|subset1|serviceA.default.svc.cluster.local"));

        // cluster is null
        serviceCluster.setClusters(null);
        clusterResources = serviceCluster.getClusterResources();
        Assert.assertNotNull(clusterResources);
        Assert.assertTrue(clusterResources.isEmpty());
    }

    @Test
    public void testIsClusterLocalityLb() {
        Assert.assertTrue(serviceCluster.isClusterLocalityLb("outbound|8080||serviceA.default.svc.cluster.local"));
        Assert.assertFalse(serviceCluster.isClusterLocalityLb("outbound|8080|subset1|serviceA.default.svc.cluster.local"));
    }

    @Test
    public void testGetPolicyOfCluster() {
        Assert.assertEquals(XdsLbPolicy.ROUND_ROBIN, serviceCluster.getLbPolicyOfCluster("outbound|8080||serviceA.default.svc.cluster.local"));
        Assert.assertEquals(XdsLbPolicy.LEAST_REQUEST, serviceCluster.getLbPolicyOfCluster("outbound|8080|subset1|serviceA.default.svc.cluster.local"));
        Assert.assertEquals(XdsLbPolicy.UNRECOGNIZED, serviceCluster.getLbPolicyOfCluster("nonexistentCluster"));
    }

    @Test
    public void testGetBaseLbPolicyOfService() {
        Assert.assertEquals(XdsLbPolicy.ROUND_ROBIN, serviceCluster.getBaseLbPolicyOfService());
    }
}
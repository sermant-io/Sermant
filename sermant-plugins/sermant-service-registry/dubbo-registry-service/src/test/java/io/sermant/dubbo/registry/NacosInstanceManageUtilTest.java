/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.dubbo.registry;

import com.alibaba.nacos.api.naming.pojo.Instance;

import io.sermant.dubbo.registry.utils.NacosInstanceManageUtil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Test NacosInstanceManageUtil
 *
 * @author chengyouling
 * @since 2022-11-29
 */
public class NacosInstanceManageUtilTest {
    private static final String serviceName = "SERVICE";
    private static final String instanceServiceName1 = "service1";
    private static final String instanceServiceName2 = "service1";
    private static final Set<String> namesSet = new HashSet<>();
    private static final List<Instance> instances = new ArrayList<>();

    /**
     * Constructor
     */
    public NacosInstanceManageUtilTest() {
        namesSet.add(instanceServiceName1);
        namesSet.add(instanceServiceName2);
        Instance instance = new Instance();
        instance.setIp("127.0.0.1");
        instances.add(instance);
    }

    /**
     * Test builds getAllCorrespondingServiceInstanceList
     */
    @Test
    public void testGetAllCorrespondingServiceInstanceList() {
        NacosInstanceManageUtil.setCorrespondingServiceNames(serviceName, namesSet);
        NacosInstanceManageUtil.initOrRefreshServiceInstanceList(instanceServiceName1, instances);
        List<Instance> list = NacosInstanceManageUtil.getAllCorrespondingServiceInstanceList(serviceName);
        Assertions.assertEquals(1, list.size());
    }

}

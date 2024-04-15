package com.huaweicloud.visibility.common;

import com.huaweicloud.visibility.entity.Consanguinity;
import com.huaweicloud.visibility.entity.Contract;
import com.huaweicloud.visibility.entity.MethodInfo;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CollectorCacheTest {
    private static final String DEFAULT_IP = "127.0.0.1";

    private static final String DEFAULT_PORT = "8080";

    /**
     * Test contract information storage and repeated storage
     */
    @Test
    public void saveContractInfo() {
        Contract contract = new Contract();
        contract.setServiceKey(CollectorCacheTest.class.getName());
        contract.setServiceType(ServiceType.DUBBO.getType());
        contract.setInterfaceName(CollectorCacheTest.class.getName());
        List<MethodInfo> methodInfoList = new ArrayList<>();
        for (Method method : CollectorCacheTest.class.getMethods()) {
            MethodInfo methodInfo = new MethodInfo();
            methodInfo.setName(method.getName());
            methodInfoList.add(methodInfo);
        }
        CollectorCache.saveContractInfo(contract);
        Assert.assertFalse(CollectorCache.CONTRACT_MAP.isEmpty());
        CollectorCache.saveContractInfo(contract);
        Assert.assertTrue(CollectorCache.CONTRACT_MAP.size() == 1);
        Assert.assertTrue(CollectorCache.CONTRACT_MAP.containsKey(CollectorCacheTest.class.getName()));
    }

    /**
     * Test kinship information is saved
     */
    @Test
    public void saveConsanguinity() {
        Consanguinity consanguinity = new Consanguinity();
        List<Contract> contractList = new ArrayList<>();
        Contract contract = new Contract();
        contract.setIp(DEFAULT_IP);
        contract.setPort(DEFAULT_PORT);
        contractList.add(contract);
        consanguinity.setProviders(contractList);
        consanguinity.setServiceKey(CollectorCacheTest.class.getName());
        CollectorCache.saveConsanguinity(consanguinity);
        Assert.assertFalse(CollectorCache.CONSANGUINITY_MAP.isEmpty());
        Assert.assertTrue(CollectorCache.CONSANGUINITY_MAP.containsKey(CollectorCacheTest.class.getName()));
    }
}
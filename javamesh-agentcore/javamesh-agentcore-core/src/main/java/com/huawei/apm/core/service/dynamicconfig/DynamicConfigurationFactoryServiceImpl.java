package com.huawei.apm.core.service.dynamicconfig;

import com.huawei.apm.core.service.CoreService;
import com.huawei.apm.core.service.dynamicconfig.nop.NopDynamicConfigurationService;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigType;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigurationFactoryService;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigurationService;
import com.huawei.apm.core.service.dynamicconfig.zookeeper.ZookeeperDynamicConfigurationService;


/**
 *
 * The implementation for the DynamicConfigurationFactoryService
 *
 */
public class DynamicConfigurationFactoryServiceImpl implements DynamicConfigurationFactoryService, CoreService {


    protected DynamicConfigurationService getDynamicConfigurationService(DynamicConfigType dct) {

        if ( dct == DynamicConfigType.ZOO_KEEPER)
            return ZookeeperDynamicConfigurationService.getInstance();

        if ( dct == DynamicConfigType.NOP)
            return NopDynamicConfigurationService.getInstance();

        return null;
    }

    @Override
    public DynamicConfigurationService getDynamicConfigurationService() {
        return this.getDynamicConfigurationService(Config.getDynamicConfigType());
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        try {
            this.getDynamicConfigurationService().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

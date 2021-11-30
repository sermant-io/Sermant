package com.huawei.javamesh.core.service.dynamicconfig;

import com.huawei.javamesh.core.common.LoggerFactory;
import com.huawei.javamesh.core.service.dynamicconfig.kie.KieDynamicConfigurationServiceImpl;
import com.huawei.javamesh.core.service.dynamicconfig.nop.NopDynamicConfigurationService;
import com.huawei.javamesh.core.service.dynamicconfig.service.DynamicConfigType;
import com.huawei.javamesh.core.service.dynamicconfig.service.DynamicConfigurationFactoryService;
import com.huawei.javamesh.core.service.dynamicconfig.service.DynamicConfigurationService;
import com.huawei.javamesh.core.service.dynamicconfig.zookeeper.ZookeeperDynamicConfigurationService;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * The implementation for the DynamicConfigurationFactoryService
 *
 */
public class DynamicConfigurationFactoryServiceImpl implements DynamicConfigurationFactoryService {

    private static final Logger logger = LoggerFactory.getLogger();

    protected DynamicConfigurationService getDynamicConfigurationService(DynamicConfigType dct) {

        if ( dct == DynamicConfigType.ZOO_KEEPER )
            return ZookeeperDynamicConfigurationService.getInstance();

        if ( dct == DynamicConfigType.KIE )
            return KieDynamicConfigurationServiceImpl.getInstance();

        if ( dct == DynamicConfigType.NOP )
            return NopDynamicConfigurationService.getInstance();

        return null;
    }

    @Override
    public DynamicConfigurationService getDynamicConfigurationService() {
        return this.getDynamicConfigurationService(Config.getDynamic_config_type());
    }

    @Override
    public void start() {
        DynamicConfigurationService dcs = this.getDynamicConfigurationService();
        logger.log(Level.INFO, "DynamicConfigurationFactoryServiceImpl start. DynamicConfigurationService inst is " +
                ( dcs == null ? "null" : dcs.toString()) );
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

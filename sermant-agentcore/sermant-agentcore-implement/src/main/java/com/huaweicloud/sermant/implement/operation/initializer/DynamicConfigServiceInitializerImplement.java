package com.huaweicloud.sermant.implement.operation.initializer;

import com.huaweicloud.sermant.core.operation.initializer.DynamicConfigServiceInitializer;
import com.huaweicloud.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.KieDynamicConfigService;
import com.huaweicloud.sermant.implement.service.dynamicconfig.zookeeper.ZooKeeperDynamicConfigService;

/**
 * 动态配置初始化实现
 *
 * @author luanwenfei
 * @since 2022-06-29
 */
public class DynamicConfigServiceInitializerImplement implements DynamicConfigServiceInitializer {
    public DynamicConfigService initKieDynamicConfigService(String serverAddress, String project){
        if(StringUtils.isBlank(serverAddress) || StringUtils.isBlank(project)){
            return new KieDynamicConfigService();
        }
        return new KieDynamicConfigService(serverAddress,project);
    }

    public DynamicConfigService initZookeeperConfigService(){
        return new ZooKeeperDynamicConfigService();
    }
}

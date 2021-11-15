package com.huawei.apm.core.service.dynamicconfig.service;

/**
 *
 * Core service for the dynamic config service.
 * This factory is used to retrieve the default configuration service.
 *
 */
public interface DynamicConfigurationFactoryService {

    public DynamicConfigurationService getDynamicConfigurationService();

}

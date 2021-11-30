package com.huawei.javamesh.core.service.dynamicconfig.service;

import com.huawei.javamesh.core.service.BaseService;

/**
 *
 * Core service for the dynamic config service.
 * This factory is used to retrieve the default configuration service.
 *
 */
public interface DynamicConfigurationFactoryService extends BaseService {

    public DynamicConfigurationService getDynamicConfigurationService();

}

package com.huawei.apm.core.service.configServer;

import com.huawei.apm.core.service.CoreService;
import org.apache.curator.framework.CuratorFramework;

public interface ConfigServer extends CoreService {

    CuratorFramework getClient();
}

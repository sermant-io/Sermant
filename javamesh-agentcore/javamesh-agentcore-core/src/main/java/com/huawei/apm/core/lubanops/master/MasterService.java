package com.huawei.apm.core.lubanops.master;

/**
 * Master Service.
 *
 * @author
 * @date 2020/10/22 17:30
 */
public interface MasterService {

    boolean register();

    int heartbeat();

}

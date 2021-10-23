package com.huawei.argus.monitor.repository;

import org.ngrinder.model.MonitoringConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by hwx683090 on 2019/4/23.
 */
public interface PerfMonitorConfigRepository extends JpaRepository<MonitoringConfig, Long>, JpaSpecificationExecutor<MonitoringConfig> {
}

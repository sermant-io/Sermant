package com.huawei.argus.monitor.service;

import org.ngrinder.model.MonitoringConfig;
import org.ngrinder.model.PerfScene;
import org.springframework.stereotype.Service;

/**
 * Created by hwx683090 on 2019/4/23.
 */
@Service
public interface IPerfMonitorConfigService {
	/**
	 * 创建一个监控配置
	 * @param monitoringConfig 监控配置实例
	 * @return 生成后的监控配置实例
	 */
	public MonitoringConfig createMonitorConfig(MonitoringConfig monitoringConfig);

	/**
	 * 更新一个监控配置
	 * @param monitoringConfig 监控配置实例
	 * @return 更新后的监控配置实例
	 */
	public MonitoringConfig updateMonitorConfig(MonitoringConfig monitoringConfig);

	/**
	 * 查询一个监控配置
	 * @param id 监控配置id
	 * @return 监控配置
	 */
	public MonitoringConfig getMonitorConfig( Long id);

}

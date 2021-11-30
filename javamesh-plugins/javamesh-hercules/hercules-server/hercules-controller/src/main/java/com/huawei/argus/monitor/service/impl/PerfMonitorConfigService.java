package com.huawei.argus.monitor.service.impl;

import com.huawei.argus.monitor.repository.PerfMonitorConfigRepository;
import com.huawei.argus.monitor.service.IPerfMonitorConfigService;
import org.ngrinder.model.MonitoringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by hwx683090 on 2019/4/23.
 */
@Service
public class PerfMonitorConfigService implements IPerfMonitorConfigService {

	@Autowired
	private PerfMonitorConfigRepository monitorConfigRepository;

	/**
	 * 创建一个监控配置
	 * @param monitoringConfig 监控配置实例
	 * @return 生成后的监控配置实例
	 */
	@Override
	public MonitoringConfig createMonitorConfig(MonitoringConfig monitoringConfig) {
		return monitorConfigRepository.save(monitoringConfig);
	}

	/**
	 * 更新一个监控配置
	 * @param monitoringConfig 监控配置实例
	 * @return 更新后的监控配置实例
	 */
	@Override
	public MonitoringConfig updateMonitorConfig(MonitoringConfig monitoringConfig) {
		return monitorConfigRepository.saveAndFlush(monitoringConfig);
	}

	/**
	 * 查询一个监控配置
	 * @param id 监控配置id
	 * @return 监控配置
	 */
	@Override
	public MonitoringConfig getMonitorConfig( Long id) {
		return monitorConfigRepository.findOne(id);
	}

}

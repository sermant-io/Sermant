package com.huawei.argus.monitor.controller;

import com.huawei.argus.monitor.service.impl.PerfMonitorConfigService;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.model.MonitoringConfig;
import org.ngrinder.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hwx683090 on 2019/4/23.
 */
@Controller
@RequestMapping("/perfMonitorConfig")
public class PerfMonitorConfigControler extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PerfMonitorConfigControler.class);

	@Autowired
	public PerfMonitorConfigService perfMonitorConfigService;

	/**
	 * 创建监控配置
	 * @param monitor 监控配置实例
	 * @return
	 */
    @RequestMapping(value = {"","/"},method = RequestMethod.POST)
    @ResponseBody
    public MonitoringConfig createMonitorConfig(@RequestBody MonitoringConfig monitor){
		return perfMonitorConfigService.createMonitorConfig(monitor);
    }

	/**
	 * 更新监控配置
	 * @param monitor 监控配置实例
	 * @return
	 */
	@RequestMapping(value = {"","/"},method = RequestMethod.PUT)
    @ResponseBody
    public MonitoringConfig updateMonitorConfig(@RequestBody MonitoringConfig monitor){
         return  perfMonitorConfigService.updateMonitorConfig(monitor);
    }

	/**
	 * 查询监控配置
	 * @param id 监控配置ID
	 * @return 监控配置
	 */
	@RequestMapping(value = "/{id}",method = RequestMethod.GET)
	@ResponseBody
	public MonitoringConfig getMonitorConfig(@PathVariable long id) {
		return perfMonitorConfigService.getMonitorConfig(id);
	}

}

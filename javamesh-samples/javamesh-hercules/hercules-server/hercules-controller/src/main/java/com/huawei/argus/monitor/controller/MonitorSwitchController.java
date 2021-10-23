package com.huawei.argus.monitor.controller;

import com.huawei.argus.monitor.common.MonitorSwitch;
import io.swagger.annotations.Api;
import org.ngrinder.model.PerfTest;
import org.ngrinder.perftest.service.PerfTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @Author: j00466872
 * @Date: 2019/5/7 19:17
 */
@Api
@RestController
@RequestMapping("/monitorSwitch")
public class MonitorSwitchController {

	@Autowired
	private MonitorSwitch monitorSwitch;

	@Autowired
	private PerfTestService perfTestService;

	@RequestMapping("/start/{id}")
	public Object startById(@PathVariable Long id) throws IOException {
		PerfTest perfTest = perfTestService.getOne(id);
		return monitorSwitch.startMonitorByPerfTest(perfTest);
	}

	@RequestMapping("/stop/{id}")
	public Object stopById(@PathVariable Long id) throws IOException {
		PerfTest perfTest = perfTestService.getOne(id);
		return monitorSwitch.stopMonitorByPerfTest(perfTest);
	}
}

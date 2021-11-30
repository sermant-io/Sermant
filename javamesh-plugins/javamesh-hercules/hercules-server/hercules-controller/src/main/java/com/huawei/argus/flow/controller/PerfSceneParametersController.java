package com.huawei.argus.flow.controller;

import com.huawei.argus.flow.service.PerfSceneParametersService;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.model.PerfScene;
import org.ngrinder.model.PerfSceneParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/perftest/scene-parameters")
public class PerfSceneParametersController extends BaseController {

	@Autowired
	PerfSceneParametersService perfSceneParametersService;

	@RequestMapping(value = {"/"},method = RequestMethod.POST)
	@ResponseBody
	public PerfSceneParameters createScene(@RequestBody PerfSceneParameters perfSceneParameters){
		System.out.println(perfSceneParameters);

		return perfSceneParametersService.create(perfSceneParameters);
	}
}

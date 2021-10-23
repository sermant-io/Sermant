package com.huawei.argus.script.controller;

import io.swagger.annotations.Api;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.model.User;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.service.FileEntryService;
import org.ngrinder.script.service.ScriptValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Author: j00466872
 * @Date: 2019/4/29 20:04
 */
@Api
@RestController
@RequestMapping("/perf_scripts")
public class ScriptController extends BaseController {

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private ScriptValidationService scriptValidationService;

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;

	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public Object list(User user){
		return fileEntryService.getAll(user);
	}

	@RequestMapping(value = "traffic", method = RequestMethod.POST)
	public Object createArgusTrafficSceneScript(User user, @RequestBody Map<String, Object> requestBody) {
		fileEntryService.prepareNewEntryForFlowTest(
			user,
			requestBody.get("sceneJson").toString(),
			requestBody.get("path").toString(),
			requestBody.get("sceneName").toString(),
			null,
			scriptHandlerFactory.getHandler("argus_traffic").getClass());
		return true;
	}
}

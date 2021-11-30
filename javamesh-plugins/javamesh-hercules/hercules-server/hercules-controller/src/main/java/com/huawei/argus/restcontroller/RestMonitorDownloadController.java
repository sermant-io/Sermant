package com.huawei.argus.restcontroller;

import org.ngrinder.agent.service.AgentPackageService;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.util.FileDownloadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

import java.io.File;

import static org.ngrinder.common.util.ExceptionUtils.processException;

@RestController
@RequestMapping("/rest/monitor")
public class RestMonitorDownloadController extends RestBaseController {

	@Autowired
	private AgentPackageService agentPackageService;


	/**
	 * Download monitor.
	 *
	 * @param fileName monitor file name.
	 * @param response response.
	 */

	@RequestMapping(value = "/download/{fileName:[a-zA-Z0-9\\.\\-_]+}")
	public void download(@PathVariable String fileName, HttpServletResponse response) {
		File home = getConfig().getHome().getDownloadDirectory();
		File monitorFile = new File(home, fileName);
		FileDownloadUtils.downloadFile(response, monitorFile);
	}

	/**
	 * Download monitor.
	 *
	 */
	@RequestMapping(value = "/download")
	public String download() {
		try {
			final File monitorPackage = agentPackageService.createMonitorPackage();
			return monitorPackage.getName();
		} catch (Exception e) {
			throw processException(e);
		}
	}

}

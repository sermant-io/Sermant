package com.huawei.argus.restcontroller;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.ngrinder.common.controller.BaseController;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;

import static org.ngrinder.common.util.CollectionUtils.buildMap;

@RestController
@RequestMapping("/rest/operation/log")
@PreAuthorize("hasAnyRole('A')")
public class RestLogMonitorController extends RestBaseController {

	private static final int LOGGER_BUFFER_SIZE = 10000;

	/**
	 * Buffer to store the latest log.
	 */
	private volatile StringBuffer stringBuffer = new StringBuffer(LOGGER_BUFFER_SIZE);

	private Tailer tailer;

	private long count = 0;
	private long modification = 0;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void init() {
		if (!isClustered()) {
			initTailer();
		}
	}

	/**
	 * Initialize the {@link Tailer}.
	 */
	private synchronized void initTailer() {
		File logFile = getLogFile();
		if (tailer != null) {
			tailer.stop();
		}
		tailer = Tailer.create(logFile, new TailerListenerAdapter() {
			/**
			 * Handles a line from a Tailer.
			 *
			 * @param line
			 *            the line.
			 */
			public void handle(String line) {
				synchronized (this) {
					if (stringBuffer.length() + line.length() > 5000) {
						count++;
						modification = 0;
						stringBuffer = new StringBuffer();
					}
					modification++;
					if (stringBuffer.length() > 0) {
						stringBuffer.append("<br>");
					}
					stringBuffer.append(line.replace("\n", "<br>"));
				}
			}
		}, 1000, true);
	}

	/**
	 * Get the log file.
	 *
	 * @return log file
	 */
	File getLogFile() {
		String logFileName = "ngrinder.log";
		return new File(getConfig().getHome().getGlobalLogFile(), logFileName);
	}

	/**
	 * Finalize bean.
	 */
	@PreDestroy
	public void destroy() {
		if (!isClustered()) {
			tailer.stop();
		}
	}

	/**
	 * Return the logger first page.
	 *
	 * @return operation/logger
	 */
	@RequestMapping("")
	public JSONObject getOne() {
		JSONObject modelInfos = new JSONObject();
		modelInfos.put("verbose", getConfig().isVerbose());
		return modelInfos;
	}

	/**
	 * Get the last log in the form of json.
	 *
	 * @return log json
	 */
	@RequestMapping("/last")
	public HttpEntity<String> getLast() {
		return toJsonHttpEntity(buildMap("index", count, "modification", modification, "log", stringBuffer));
	}

	/**
	 * Turn on verbose log mode.
	 *
	 * @param verbose true if verbose mode
	 * @return success message if successful
	 */
	@RequestMapping("/verbose")
	public HttpEntity<String> enableVerbose(@RequestParam(value = "verbose", defaultValue = "false") Boolean verbose) {
		getConfig().initLogger(verbose);
		initTailer();
		return toJsonHttpEntity(buildMap("success", true));
	}

}

package com.huawei.argus.restcontroller;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.common.constant.ControllerConstants;
import org.ngrinder.common.controller.BaseController;
import org.ngrinder.common.util.ThreadUtils;
import org.ngrinder.home.model.PanelEntry;
import org.ngrinder.home.service.HomeService;
import org.ngrinder.infra.logger.CoreLogger;
import org.ngrinder.infra.schedule.ScheduledTaskService;
import org.ngrinder.model.Role;
import org.ngrinder.model.User;
import org.ngrinder.region.service.RegionService;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.http.HttpEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static org.ngrinder.common.util.ExceptionUtils.processException;
import static org.ngrinder.common.util.Preconditions.checkNotNull;

@RestController
@RequestMapping("/rest")
public class RestHomeController extends RestBaseController implements ControllerConstants {

	private static final Logger LOG = LoggerFactory.getLogger(RestHomeController.class);

	@Autowired
	private HomeService homeService;

	@Autowired
	private RegionService regionService;

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;

	private static final String TIMEZONE_ID_PREFIXES = "^(Africa|America|Asia|Atlantic|"
		+ "Australia|Europe|Indian|Pacific)/.*";

	private List<TimeZone> timeZones = null;

	@Autowired
	private ScheduledTaskService scheduledTaskService;

	private static Gson rawObjectJsonSerializer = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * Initialize {@link org.ngrinder.home.controller.HomeController}.
	 */
	@PostConstruct
	public void init() {
		timeZones = new ArrayList<TimeZone>();
		final String[] timeZoneIds = TimeZone.getAvailableIDs();
		for (final String id : timeZoneIds) {
			if (id.matches(TIMEZONE_ID_PREFIXES) && !TimeZone.getTimeZone(id).getDisplayName().contains("GMT")) {
				timeZones.add(TimeZone.getTimeZone(id));
			}
		}
		Collections.sort(timeZones, new Comparator<TimeZone>() {
			public int compare(final TimeZone a, final TimeZone b) {
				return a.getID().compareTo(b.getID());
			}
		});
		scheduledTaskService.runAsync(new Runnable() {
			@Override
			public void run() {
				getLeftPanelEntries();
				getRightPanelEntries();
			}
		});
	}

	/**
	 * Return nGrinder index page.
	 *
	 * @param user      user
	 * @param exception exception if it's redirected from exception handler
	 * @param region    region. where this access comes from. it's optional
	 * @param response  response
	 * @param request   request
	 * @return "index" if already logged in. Otherwise "login".
	 */
	@RequestMapping(value = {"/home", "/"})
	public JSONObject home(User user, @RequestParam(value = "exception", defaultValue = "") String exception,
					   @RequestParam(value = "region", defaultValue = "") String region,
					   HttpServletResponse response, HttpServletRequest request) {
		JSONObject modelInfos = new JSONObject();
		try {
			Role role;
			try {
				recordReferrer(region);
				// set local language
				setLanguage(getCurrentUser().getUserLanguage(), response, request);
				addTimezoneLoginAttribute(modelInfos);
				addCommonLoginAttribute(modelInfos);
				role = user.getRole();
			} catch (AuthenticationCredentialsNotFoundException e) {
				//return "login";
				modelInfos.put("login", LoginController.LOGIN_FAILURE);
				return modelInfos;
			}
			setPanelEntries(modelInfos);
			modelInfos.put("handlers", scriptHandlerFactory.getVisibleHandlers());

			if (StringUtils.isNotBlank(exception)) {
				modelInfos.put("exception", exception);
			}
			if (role == Role.ADMIN || role == Role.SUPER_USER || role == Role.USER) {
				//return "index";
				modelInfos.put("login", LoginController.LOGIN_SUCCESS);
				return modelInfos;
			} else {
				LOG.info("Invalid user role:{}", role.getFullName());
				//return "login";
				modelInfos.put("login", LoginController.LOGIN_FAILURE);
				return modelInfos;
			}
		} catch (Exception e) {
			// Make the home reliable...
			modelInfos.put("exception", e.getMessage());
			//return "index";
			modelInfos.put("login", LoginController.LOGIN_FAILURE);
			return modelInfos;
		}
	}

	private void setPanelEntries(JSONObject modelInfos) {
		modelInfos.put("left_panel_entries", getLeftPanelEntries());
		modelInfos.put("right_panel_entries", getRightPanelEntries());
		modelInfos.put(
			"ask_question_url",
			getConfig().getControllerProperties().getProperty(PROP_CONTROLLER_FRONT_PAGE_ASK_QUESTION_URL,
				getMessages(PROP_CONTROLLER_FRONT_PAGE_ASK_QUESTION_URL)));
		modelInfos.put(
			"see_more_question_url",
			getConfig().getControllerProperties().getProperty(PROP_CONTROLLER_FRONT_PAGE_QNA_MORE_URL,
				getMessages(PROP_CONTROLLER_FRONT_PAGE_QNA_MORE_URL)));
		modelInfos.put("see_more_resources_url", getConfig().getControllerProperties().getProperty
			(PROP_CONTROLLER_FRONT_PAGE_RESOURCES_MORE_URL));


	}

	private List<PanelEntry> getRightPanelEntries() {
		if (getConfig().getControllerProperties().getPropertyBoolean(PROP_CONTROLLER_FRONT_PAGE_ENABLED)) {
			// Get nGrinder Resource RSS
			String rightPanelRssURL = getConfig().getControllerProperties().getProperty(PROP_CONTROLLER_FRONT_PAGE_RESOURCES_RSS);
			return homeService.getRightPanelEntries(rightPanelRssURL);
		}
		return Collections.emptyList();
	}

	private List<PanelEntry> getLeftPanelEntries() {
		if (getConfig().getControllerProperties().getPropertyBoolean(PROP_CONTROLLER_FRONT_PAGE_ENABLED)) {
			// Make the i18n applied QnA panel. Depending on the user language, show the different QnA panel.
			String leftPanelRssURLKey = getMessages(PROP_CONTROLLER_FRONT_PAGE_QNA_RSS);
			// Make admin configure the QnA panel.
			String leftPanelRssURL = getConfig().getControllerProperties().getProperty(PROP_CONTROLLER_FRONT_PAGE_QNA_RSS,
				leftPanelRssURLKey);
			return homeService.getLeftPanelEntries(leftPanelRssURL);
		}
		return Collections.emptyList();
	}

	private void recordReferrer(String region) {
		if (StringUtils.isNotEmpty(region)) {
			CoreLogger.LOGGER.info("Accessed to {}", region);
		}
	}

	/**
	 * Return the health check message. If there is shutdown lock, it returns
	 * 503. Otherwise it returns region lists.
	 *
	 * @param response response
	 * @return region list
	 */
	@RequestMapping("/check/healthcheck")
	public HttpEntity<String> healthCheck(HttpServletResponse response) {
		if (getConfig().hasShutdownLock()) {
			try {
				response.sendError(503, "nGrinder is about to down");
			} catch (IOException e) {
				LOG.error("While running healthCheck() in HomeController, the error occurs.");
				LOG.error("Details : ", e);
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("current", regionService.getCurrent());
		map.put("regions", regionService.getAll());
		return toJsonHttpEntity(map, rawObjectJsonSerializer);
	}

	/**
	 * Return health check message with 1 sec delay. If there is shutdown lock,
	 * it returns 503. Otherwise, it returns region lists.
	 *
	 * @param sleep    in milliseconds.
	 * @param response response
	 * @return region list
	 */
	@ResponseBody
	@RequestMapping("/check/healthcheck_slow")
	public HttpEntity<String> healthCheckSlowly(@RequestParam(value = "delay", defaultValue = "1000") int sleep,
												HttpServletResponse response) {
		ThreadUtils.sleep(sleep);
		return healthCheck(response);
	}

	private void setLanguage(String lan, HttpServletResponse response, HttpServletRequest request) {
		LocaleResolver localeResolver = checkNotNull(RequestContextUtils.getLocaleResolver(request),
			"No LocaleResolver found!");
		LocaleEditor localeEditor = new LocaleEditor();
		String language = StringUtils.defaultIfBlank(lan,
			getConfig().getControllerProperties().getProperty(PROP_CONTROLLER_DEFAULT_LANG));
		localeEditor.setAsText(language);
		localeResolver.setLocale(request, response, (Locale) localeEditor.getValue());
	}

	/**
	 * Return the login page.
	 *
	 * @return "login" if not logged in. Otherwise, "/"
	 */
	@RequestMapping(value = "/login")
	public JSONObject login() {
		JSONObject modelInfos = new JSONObject();
		addTimezoneLoginAttribute(modelInfos);
		addCommonLoginAttribute(modelInfos);
		modelInfos.put("login", LoginController.LOGIN_SUCCESS);
		try {
			getCurrentUser();
		} catch (Exception e) {
			CoreLogger.LOGGER.info("Login Failure " + e.getMessage());
			modelInfos.put("login", LoginController.LOGIN_FAILURE);
		}
		return modelInfos;
	}

	private void addCommonLoginAttribute(JSONObject modelInfos) {
		modelInfos.put("signUpEnabled", getConfig().isSignUpEnabled());
		final String defaultLang = getConfig().getControllerProperties().getProperty(ControllerConstants.PROP_CONTROLLER_DEFAULT_LANG);
		modelInfos.put("defaultLang", defaultLang);
	}

	private void addTimezoneLoginAttribute(JSONObject modelInfos) {
		TimeZone defaultTime = TimeZone.getDefault();
		modelInfos.put("timezones", timeZones);
		modelInfos.put("defaultTime", defaultTime.getID());
	}

	/**
	 * Error redirection to 404.[未改变，不使用]
	 *
	 * @return "redirect:/doError"
	 */
	@RequestMapping(value = "/error_404")
	public String error404(ModelMap model) {
		model.clear();
		return "redirect:/doError?type=404";
	}

	/**
	 * Error redirection as a second phase.
	 *
	 * @param user     user
	 * @param response response
	 * @param request  request
	 * @return "index"
	 */
	@RequestMapping(value = "/doError")
	public JSONObject second(User user, HttpServletResponse response, HttpServletRequest request) {
		String parameter = request.getParameter("type");
		JSONObject modelInfos  = home(user, null, null, response, request);
		if ("404".equals(parameter)) {
			modelInfos .put("exception", processException("Requested URL does not exist"));
		}
		return modelInfos;
	}

}


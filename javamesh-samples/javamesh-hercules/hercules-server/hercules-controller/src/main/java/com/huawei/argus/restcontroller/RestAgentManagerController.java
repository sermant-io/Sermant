package com.huawei.argus.restcontroller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Predicate;
import com.huawei.argus.common.PageModel;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.agent.service.AgentPackageService;
import org.ngrinder.common.controller.RestAPI;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.ngrinder.region.model.RegionInfo;
import org.ngrinder.region.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Collections2.filter;
import static org.ngrinder.common.util.CollectionUtils.*;
import static org.ngrinder.common.util.SpringSecurityUtils.containsAuthority;
import static org.ngrinder.common.util.SpringSecurityUtils.getCurrentAuthorities;

@RestController
@RequestMapping("/rest/agent")
@PreAuthorize("hasAnyRole('A', 'S')")
public class RestAgentManagerController extends RestBaseController {

	@SuppressWarnings("SpringJavaAutowiringInspection")
	@Autowired
	private AgentManagerService agentManagerService;

	@Autowired
	private RegionService regionService;

	@Autowired
	private AgentPackageService agentPackageService;

	/**
	 * Get the agents.
	 */
	@RequestMapping({"/list"})
	@PreAuthorize("hasAnyRole('A', 'S', 'U')")
	public JSONObject getAll(final User user, @RequestParam(value = "region", required = false) final String region) {
		final Collection<? extends GrantedAuthority> authorities = getCurrentAuthorities();
		Collection<AgentInfo> agents = agentManagerService.getAllVisible();

		agents = filter(agents, new Predicate<AgentInfo>() {
			@Override
			public boolean apply(AgentInfo agentInfo) {
				return filterAgentByCluster(region, agentInfo.getRegion());
			}
		});

		agents = filter(agents, new Predicate<AgentInfo>() {
			@Override
			public boolean apply(AgentInfo agentInfo) {
				return filterAgentByUserAuthorityAndId(authorities, user.getUserId(), region, agentInfo.getRegion());
			}
		});

		JSONObject modelInfos = new JSONObject();
		modelInfos.put("agents", listToJsonArray(Arrays.asList(agents.toArray())));
		modelInfos.put("region", region);
		modelInfos.put("regions", regionService.getAllVisibleRegionNames());
		File agentPackage = null;
		if (isClustered()) {
			if (StringUtils.isNotBlank(region)) {
				final RegionInfo regionInfo = regionService.getOne(region);
				agentPackage = agentPackageService.createAgentPackage(region, regionInfo.getIp(), regionInfo.getControllerPort(), null);
			}
		} else {
			agentPackage = agentPackageService.createAgentPackage("", "", getConfig().getControllerPort(), null);
		}
		if (agentPackage != null) {
			modelInfos.put("downloadFileName", agentPackage.getName());
		}
		return modelInfos;
	}

	/**
	 * Get the agents by page.
	 */
	@RequestMapping(value = {"/list"}, params = "pageSize")
	@PreAuthorize("hasAnyRole('A', 'S', 'U')")
	public JSONObject getAgentPage(final User user,
								   @RequestParam int pageSize,
								   @RequestParam int current,
								   @RequestParam(required = false) String sorter,
								   @RequestParam(required = false) String order,
								   @RequestParam(value = "region", required = false) final String region) {
		final Collection<? extends GrantedAuthority> authorities = getCurrentAuthorities();
		PageModel<AgentInfo> agentInfoPage =
			agentManagerService.getAgentInfoPage(pageSize, current, sorter, order, region);
		Collection<AgentInfo> agents = agentInfoPage.getPageContent();

		// 过滤用户能查询的agent，权限问题
		agents = filter(agents, new Predicate<AgentInfo>() {
			@Override
			public boolean apply(AgentInfo agentInfo) {
				return filterAgentByUserAuthorityAndId(authorities, user.getUserId(), region, agentInfo.getRegion());
			}
		});

		// 封装返回结果
		JSONObject modelInfos = new JSONObject();
		modelInfos.put("total", agentInfoPage.getTotalCount());
		modelInfos.put("data", listToJsonArray(Arrays.asList(agents.toArray())));
		modelInfos.put("totalPages", agentInfoPage.getTotalPages());
		return modelInfos;
	}

	/**
	 * Filter agent list by referring to cluster
	 */
	private boolean filterAgentByCluster(String region, String agentRegion) {
		//noinspection SimplifiableIfStatement
		if (StringUtils.isEmpty(region)) {
			return true;
		} else {
			return agentRegion.startsWith(region + "_owned") || region.equals(agentRegion);
		}
	}

	/**
	 * Filter agent list using user authority and user id
	 */
	private boolean filterAgentByUserAuthorityAndId(Collection<? extends GrantedAuthority> authorities, String userId, String region, String agentRegion) {
		if (isAdminOrSuperUser(authorities)) {
			return true;
		}

		if (StringUtils.isEmpty(region)) {
			return !agentRegion.contains("_owned_") || agentRegion.endsWith("_owned_" + userId);
		} else {
			return agentRegion.startsWith(region + "_owned_" + userId) || region.equals(agentRegion);
		}
	}

	/**
	 * Check if the current user is admin or super user
	 */
	private boolean isAdminOrSuperUser(Collection<? extends GrantedAuthority> authorities) {
		return containsAuthority(authorities, "A") || containsAuthority(authorities, "S");
	}

	/**
	 * Get the agent detail info for the given agent id.[方法重新命名，用于与另一个接口区分]
	 *
	 * @param id agent id
	 * @return agent/agentDetail
	 */
	@RequestMapping(value = {"/{id}"}, method = RequestMethod.GET)
	public JSONObject getOneById(@PathVariable Long id) {
		JSONObject modelInfos = new JSONObject();
		AgentInfo agentInfo = agentManagerService.getOne(id);
		modelInfos.put("agent", modelStrToJson(agentInfo == null ? "" : agentInfo.toString()));
		return modelInfos;
	}

	/**
	 * Clean up the agents in the inactive region
	 */

	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api", params = "action=cleanup", method = RequestMethod.POST)
	public HttpEntity<String> cleanUpAgentsInInactiveRegion() {
		agentManagerService.cleanup();
		return successJsonHttpEntity();
	}

	/**
	 * Get the current performance of the given agent.
	 *
	 * @param id   agent id
	 * @param ip   agent ip
	 * @param name agent name
	 * @return json message
	 */

	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping("/api/{id}/state")
	public HttpEntity<String> getState(@PathVariable Long id, @RequestParam String ip, @RequestParam String name) {
		agentManagerService.requestShareAgentSystemDataModel(id);
		return toJsonHttpEntity(agentManagerService.getSystemDataModel(ip, name));
	}

	/**
	 * Get the current all agents state.
	 *
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A', 'S', 'U')")
	@RequestMapping(value = {"/api/states"}, method = RequestMethod.GET)
	public HttpEntity<String> getStates() {
		List<AgentInfo> agents = agentManagerService.getAllVisible();
		return toJsonHttpEntity(getAgentStatus(agents));
	}

	/**
	 * Get all agents from database.
	 *
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = {"/api"}, method = RequestMethod.GET)
	public HttpEntity<String> getAll() {
		return toJsonHttpEntity(agentManagerService.getAllVisible());
	}

	/**
	 * Get the agent for the given agent id.
	 *
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{id}", method = RequestMethod.GET)
	public HttpEntity<String> getOne(@PathVariable("id") Long id) {
		return toJsonHttpEntity(agentManagerService.getOne(id));
	}

	/**
	 * Approve an agent.
	 *
	 * @param id agent id
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{id}", params = "action=approve", method = RequestMethod.PUT)
	public HttpEntity<String> approve(@PathVariable("id") Long id) {
		agentManagerService.approve(id, true);
		return successJsonHttpEntity();
	}

	/**
	 * Disapprove an agent.
	 *
	 * @param id agent id
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{id}", params = "action=disapprove", method = RequestMethod.PUT)
	public HttpEntity<String> disapprove(@PathVariable("id") Long id) {
		agentManagerService.approve(id, false);
		return successJsonHttpEntity();
	}

	/**
	 * Stop the given agent.
	 *
	 * @param id agent id
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{id}", params = "action=stop", method = RequestMethod.PUT)
	public HttpEntity<String> stop(@PathVariable("id") Long id) {
		agentManagerService.stopAgent(id);
		return successJsonHttpEntity();
	}

	/**
	 * Stop the given agent.
	 *
	 * @param ids comma separated agent id list
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api", params = "action=stop", method = RequestMethod.PUT)
	public HttpEntity<String> stop(@RequestParam("ids") String ids) {
		String[] split = StringUtils.split(ids, ",");
		for (String each : split) {
			stop(Long.parseLong(each));
		}
		return successJsonHttpEntity();
	}

	/**
	 * Update the given agent.
	 *
	 * @param id agent id
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{id}", params = "action=update", method = RequestMethod.PUT)
	public HttpEntity<String> update(@PathVariable("id") Long id) {
		agentManagerService.update(id);
		return successJsonHttpEntity();
	}

	/**
	 * Send update message to agent side
	 *
	 * @param ids comma separated agent id list
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api", params = "action=update", method = RequestMethod.PUT)
	public HttpEntity<String> update(@RequestParam("ids") String ids) {
		String[] split = StringUtils.split(ids, ",");
		for (String each : split) {
			update(Long.parseLong(each));
		}
		return successJsonHttpEntity();
	}

	/**
	 * Delete the given agent.
	 *
	 * @param id agent id
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api/{id}", params = "action=delete", method = RequestMethod.DELETE)
	HttpEntity<String> deleteOne(@PathVariable("id") Long id) {
		agentManagerService.delete(id);
		return successJsonHttpEntity();
	}

	/**
	 * Delete the given agents
	 *
	 * @param ids comma separated agent id list
	 * @return json message
	 */
	@RestAPI
	@PreAuthorize("hasAnyRole('A')")
	@RequestMapping(value = "/api", params = "action=delete", method = RequestMethod.DELETE)
	HttpEntity<String> deleteMany(@RequestParam("ids") String ids) {
		String[] split = StringUtils.split(ids, ",");
		for (String each : split) {
			deleteOne(Long.parseLong(each));
		}
		return successJsonHttpEntity();
	}

	private List<Map<String, Object>> getAgentStatus(List<AgentInfo> agents) {
		List<Map<String, Object>> statuses = newArrayList(agents.size());
		for (AgentInfo each : agents) {
			Map<String, Object> result = newHashMap();
			result.put("id", each.getId());
			result.put("port", each.getPort());
			result.put("icon", each.getState().getCategory().getIconName());
			result.put("state", each.getState());
			statuses.add(result);
		}
		return statuses;
	}

	/**
	 * Get the number of available agents.
	 *
	 * @param user         The login user
	 * @param targetRegion The name of target region
	 * @return availableAgentCount Available agent count
	 */
	@RestAPI
	@RequestMapping(value = {"/api/availableAgentCount"}, method = RequestMethod.GET)
	@PreAuthorize("permitAll")
	public HttpEntity<String> getAvailableAgentCount(User user,
													 @RequestParam(value = "targetRegion") String targetRegion) {
		int availableAgentCount = agentManagerService.getReadyAgentCount(user, targetRegion);
		return toJsonHttpEntity(buildMap("availableAgentCount", availableAgentCount));
	}

}


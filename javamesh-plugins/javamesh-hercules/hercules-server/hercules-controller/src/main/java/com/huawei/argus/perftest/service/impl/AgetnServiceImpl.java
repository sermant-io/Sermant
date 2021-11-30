package com.huawei.argus.perftest.service.impl;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.huawei.argus.perftest.service.IAgentInfoService;
import net.grinder.message.console.AgentControllerState;
import org.apache.commons.lang.StringUtils;
import org.ngrinder.agent.repository.AgentManagerRepository;
import org.ngrinder.agent.service.AgentManagerService;
import org.ngrinder.agent.service.LocalAgentService;
import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Collections2.filter;
import static org.ngrinder.common.util.SpringSecurityUtils.containsAuthority;

/**
 * @author lWX716491
 * @date 2019/04/24 10:04
 */
@Service
public class AgetnServiceImpl implements IAgentInfoService {
	@Autowired
	private AgentManagerService agentManagerService;

	@Autowired
	protected LocalAgentService cachedLocalAgentService;

	@Autowired
	private AgentManagerRepository agentManagerRepository;

	@Override
	public Collection<AgentInfo> getAgentInfoCollection(User user,final String region) {
		final String userId = user.getUserId();
//		final Collection<? extends GrantedAuthority> authorities = getCurrentAuthorities();
		Collection<AgentInfo> agents = this.getAllVisible();
		agents = filter(agents, new Predicate<AgentInfo>() {
			@Override
			public boolean apply(AgentInfo agentInfo) {
				return filterAgentByCluster(region, agentInfo.getRegion());
			}
		});
//		agents = filter(agents, new Predicate<AgentInfo>() {
//			@Override
//			public boolean apply(AgentInfo agentInfo) {
//				return filterAgentByUserAuthorityAndId(authorities, userId, region, agentInfo.getRegion());
//			}
//		});
		return agents;
	}
	@Override
	public List<AgentInfo> getAllVisible() {
		List<AgentInfo> agents = Lists.newArrayList();
		for (AgentInfo agentInfo : getAllLocal()) {
			final AgentControllerState state = agentInfo.getState();
			Boolean approved = agentInfo.getApproved();
			if (state != null && state.isActive() && approved) {
				agents.add(agentInfo);
			}
		}
		return agents;
	}

	@Override
	public Collection<AgentInfo> getSelectedAgents(String agentIds) {
		if (agentIds == null || agentIds.length() <= 0)
			return null;
		String[] split = agentIds.split(",");
		List<String> ids = Arrays.asList(split);
		return agentManagerRepository.getSelectedAgents(ids);

	}

	public List<AgentInfo> getAllLocal() {
		return Collections.unmodifiableList(cachedLocalAgentService.getLocalAgents());
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
			return agentRegion.startsWith(region + "_owned_" + userId) ||  region.equals(agentRegion);
		}
	}
	/**
	 * Check if the current user is admin or super user
	 */
	private boolean isAdminOrSuperUser(Collection<? extends GrantedAuthority> authorities) {
		return containsAuthority(authorities, "A") || containsAuthority(authorities, "S");
	}




}

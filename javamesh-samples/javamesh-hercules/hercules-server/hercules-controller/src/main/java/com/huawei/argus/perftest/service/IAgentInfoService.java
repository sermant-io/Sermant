package com.huawei.argus.perftest.service;

import org.ngrinder.model.AgentInfo;
import org.ngrinder.model.User;

import java.util.Collection;
import java.util.List;

/**
 * @author lWX716491
 * @date 2019/04/24 10:04
 */
public interface IAgentInfoService {
	Collection<AgentInfo> getAgentInfoCollection(User user,final String region);

	List<AgentInfo> getAllVisible();

	Collection<AgentInfo> getSelectedAgents(String agentIds);
}

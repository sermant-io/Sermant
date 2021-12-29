package com.huawei.emergency.service.impl;

import com.huawei.emergency.entity.EmergencyAgent;
import com.huawei.emergency.entity.EmergencyAgentExample;
import com.huawei.emergency.mapper.EmergencyAgentMapper;
import com.huawei.emergency.service.EmergencyAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmergencyAgentServiceImpl implements EmergencyAgentService {
    @Autowired
    private EmergencyAgentMapper mapper;

    @Override
    public void addAgent(String ip, String port) {
        EmergencyAgent agent = new EmergencyAgent();
        agent.setIp(ip);
        agent.setPort(port);
        agent.setStatus("READY");
        mapper.insert(agent);
    }

    @Override
    public void removeAgent(String ip){
        EmergencyAgentExample example = new EmergencyAgentExample();
        example.createCriteria().andIpEqualTo(ip);
        mapper.deleteByExample(example);
    }
}

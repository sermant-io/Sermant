package com.huawei.emergency.service;

public interface EmergencyAgentService {
    void addAgent(String ip,String port);
    void removeAgent(String ip);
}

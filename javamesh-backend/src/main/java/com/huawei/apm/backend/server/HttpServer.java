package com.huawei.apm.backend.server;

import com.alibaba.fastjson.JSONObject;
import com.huawei.apm.backend.entity.*;
import com.huawei.apm.backend.util.RandomUtil;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/apm2")
public class HttpServer {

    RandomUtil RANDOM_UTIL = new RandomUtil();
    private final Integer MIN = 1;
    private final Integer MAX = 10;

    private final Long random_long = RANDOM_UTIL.getRandomLong(MIN, MAX);
    private final Integer random_int = RANDOM_UTIL.getRandomInt(MAX);
    private final String random_str = RANDOM_UTIL.getRandomStr(MAX);


    @PostMapping("/master/v1/register")
    public String invokePost(@RequestBody JSONObject jsonParam) {
        RegisterResult registerResult = new RegisterResult();
        registerResult.setAppId(random_long);
        registerResult.setEnvId(random_long);
        registerResult.setDomainId(random_int);
        registerResult.setAgentVersion(random_str);
        registerResult.setInstanceId(random_long);
        registerResult.setBusinessId(random_long);
        return JSONObject.toJSONString(registerResult);
    }

    @PostMapping("/master/v1/heartbeat")
    public String invokePost() {

        Hashtable<String, String> map = new Hashtable<>();

        HeartBeatResult heartBeatResult = new HeartBeatResult();
        heartBeatResult.setHeartBeatInterval(random_int);
        heartBeatResult.setAttachment(map);
        heartBeatResult.setMonitorItemList(Collections.singletonList(getMonitorItem(map)));
        heartBeatResult.setSystemProperties(map);
        heartBeatResult.setAccessAddressList(Collections.singletonList(getAddress()));
        heartBeatResult.setInstanceStatus(random_int);
        heartBeatResult.setBusinessId(random_long);
        heartBeatResult.setMd5(random_str);
        return JSONObject.toJSONString(heartBeatResult);
    }

    public MonitorItem getMonitorItem(Hashtable<String, String> map) {
        MonitorItem monitorItem = new MonitorItem();
        monitorItem.setCollectorName(random_str);
        monitorItem.setInterval(random_int);
        monitorItem.setCollectorId(random_int);
        monitorItem.setMonitorItemId(random_long);
        monitorItem.setStatus(random_int);
        monitorItem.setParameters(map);
        return monitorItem;
    }

    public Address getAddress() {
        Address address = new Address();
        address.setHost(random_str);
        address.setPort(random_int);
        address.setSport(random_int);
        address.setType(AddressType.access);
        address.setScope(AddressScope.outer);
        address.setProtocol(Protocol.WS);
        return address;
    }
}

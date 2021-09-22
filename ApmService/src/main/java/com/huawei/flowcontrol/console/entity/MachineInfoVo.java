package com.huawei.flowcontrol.console.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 此处部分引用alibaba/Sentinel开源社区代码，诚挚感谢alibaba/Sentinel开源团队的慷慨贡献
 */
@Getter
@Setter
public class MachineInfoVo extends MachineInfo {
    private boolean healthy;

    public static List<MachineInfoVo> fromMachineInfoList(List<MachineInfo> machines) {
        List<MachineInfoVo> list = new ArrayList<>();
        for (MachineInfo machine : machines) {
            list.add(fromMachineInfo(machine));
        }
        return list;
    }

    public static MachineInfoVo fromMachineInfo(MachineInfo machine) {
        MachineInfoVo vo = new MachineInfoVo();
        vo.setApp(machine.getApp());
        vo.setHostname(machine.getHostname());
        vo.setIp(machine.getIp());
        vo.setPort(machine.getPort());
        vo.setLastHeartbeat(machine.getLastHeartbeat());
        vo.setHeartbeatVersion(machine.getHeartbeatVersion());
        vo.setVersion(machine.getVersion());
        vo.setHealthy(machine.isHealthy());
        return vo;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

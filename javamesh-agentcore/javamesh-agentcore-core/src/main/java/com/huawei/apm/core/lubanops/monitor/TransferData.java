package com.huawei.apm.core.lubanops.monitor;

import java.util.ArrayList;
import java.util.List;

/**
 * transfer信息 <br>
 *
 * @author zWX482523
 * @since 2018年3月3日
 */
public class TransferData {

    private List<String> innerIpList = new ArrayList<String>();

    private List<String> outerIpList = new ArrayList<String>();

    private String host;

    public List<String> getInnerIpList() {
        return innerIpList;
    }

    public void setInnerIpList(List<String> innerIpList) {
        this.innerIpList = innerIpList;
    }

    public void addInnerIp(String innerIp) {
        innerIpList.add(innerIp);
    }

    public List<String> getOuterIpList() {
        return outerIpList;
    }

    public void setOuterIpList(List<String> outerIpList) {
        this.outerIpList = outerIpList;
    }

    public void addOuterIp(String outerIp) {
        outerIpList.add(outerIp);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 根据类型获取ip <br>
     *
     * @param type
     * @return
     * @author zWX482523
     * @since 2018年3月3日
     */
    public List<String> getIpListByType(String type) {
        if ("outer".equals(type)) {
            return outerIpList;
        } else if ("inner".equals(type)) {
            return innerIpList;
        }
        return new ArrayList<String>();
    }

}

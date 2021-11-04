package com.huawei.route.common.report.common.entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.HashMap;
import java.util.Map;

public class Service {
    private ServiceStatus serviceStatus;

    private String serviceName;

    @JSONField(name = "status")
    public int getStatus() {
        return serviceStatus.getStatus();
    }

    @JSONField(name = "status")
    public void setStatus(int status) {
        this.serviceStatus = ServiceStatus.getEnum(status);
    }

    @JSONField(serialize = false)
    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    @JSONField(serialize = false)
    public void setServiceStatus(ServiceStatus serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public enum ServiceStatus {
        /**
         * 正常
         */
        UP(1),

        /**
         * 下线
         */
        DOWN(0);

        private static final Map<Integer, ServiceStatus> CODE_MAP = new HashMap<Integer, ServiceStatus>();

        static {
            for (ServiceStatus typeEnum : ServiceStatus.values()) {
                CODE_MAP.put(typeEnum.getStatus(), typeEnum);
            }
        }

        public int getStatus() {
            return status;
        }

        public static ServiceStatus getEnum(int status) {
            return CODE_MAP.get(status);
        }

        ServiceStatus(int status) {
            this.status = status;
        }

        private final int status;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}


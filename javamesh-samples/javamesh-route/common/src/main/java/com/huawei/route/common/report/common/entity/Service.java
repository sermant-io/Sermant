package com.huawei.route.common.report.common.entity;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务
 */
public class Service {
    private ServiceStatus serviceStatus;

    private String serviceName;

    /**
     * 获取服务状态
     *
     * @return 服务状态
     */
    @JSONField(name = "status")
    public int getStatus() {
        return serviceStatus.getStatus();
    }

    /**
     * 设置服务状态
     *
     * @param status 状态
     */
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

    /**
     * 服务状态
     */
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

        /**
         * 获取状态
         *
         * @return 状态
         */
        public int getStatus() {
            return status;
        }

        /**
         * 获取服务状态
         *
         * @param status 状态
         * @return ServiceStatus
         */
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


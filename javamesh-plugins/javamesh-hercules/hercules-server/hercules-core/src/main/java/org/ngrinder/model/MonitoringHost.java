/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */

package org.ngrinder.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 功能描述：需要监控的服务器配置
 *
 *
 * @since 2021-11-11
 */
@SuppressWarnings({"JpaDataSourceORMInspection"})
@Entity
@Table(name = "MONITORING_HOST")
public class MonitoringHost extends BaseEntity<MonitoringHost> {
	@ManyToOne(targetEntity = PerfTest.class)
	@JsonIgnore
	@Expose(serialize = false)
	@JoinColumn(name = "test_id", referencedColumnName = "id")
	private PerfTest perfTest;

	@Expose
	@Cloneable
	@Column(name = "host")
	private String host;

	@Expose
	@Cloneable
	@Column(name = "ip")
	private String ip;

	@Expose
	@Cloneable
	@Column(name = "jvm_type")
	private String jvmType;

	@Expose
	@Cloneable
	@Column(name = "monitor_jvm")
	private boolean monitorJvm;

	public PerfTest getPerfTest() {
		return perfTest;
	}

	public void setPerfTest(PerfTest perfTest) {
		this.perfTest = perfTest;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getJvmType() {
		return jvmType;
	}

	public void setJvmType(String desc) {
		this.jvmType = desc;
	}

	public boolean getMonitorJvm() {
		return monitorJvm;
	}

	public void setMonitorJvm(boolean monitorJvm) {
		this.monitorJvm = monitorJvm;
	}
}

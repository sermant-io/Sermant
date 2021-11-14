/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
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
 * @author z30009938
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
	@Column(name = "tags")
	private String tags;

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

	public String getTags() {
		return tags;
	}

	public void setTags(String desc) {
		this.tags = desc;
	}
}

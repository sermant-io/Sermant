package org.ngrinder.model;

import com.google.gson.annotations.Expose;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


@SuppressWarnings({"JpaDataSourceORMInspection"})
@Entity
@Table(name = "TRAFFIC_CHOOSE")
public class TrafficChoose extends BaseEntity<TrafficChoose> {

	private static final int MAX_LONG_STRING_SIZE = 9990;

	private static final int MAX_STRING_SIZE = 2048;

	public TrafficChoose() {

	}

	@Expose
	@Cloneable
	@Column(name = "app_id")
	private Integer appId;

	@Expose
	@Cloneable
	@Column(name = "execute_ids", length = MAX_STRING_SIZE)
	private String executeIds;

	@Expose
	@Cloneable
	@Column(name = "record_task_ids", length = MAX_STRING_SIZE)
	private String recordTaskIds;

	@Expose
	@Cloneable
	@Column(name = "execute_nos", length = MAX_STRING_SIZE)
	private String executeNos;

	public Integer getAppId() {
		return appId;
	}

	public void setAppId(Integer appId) {
		this.appId = appId;
	}

	public String getExecuteIds() {
		return executeIds;
	}

	public void setExecuteIds(String executeIds) {
		this.executeIds = executeIds;
	}

	public String getRecordTaskIds() {
		return recordTaskIds;
	}

	public void setRecordTaskIds(String recordTaskIds) {
		this.recordTaskIds = recordTaskIds;
	}

	public String getExecuteNos() {
		return executeNos;
	}

	public void setExecuteNos(String executeNos) {
		this.executeNos = executeNos;
	}
}

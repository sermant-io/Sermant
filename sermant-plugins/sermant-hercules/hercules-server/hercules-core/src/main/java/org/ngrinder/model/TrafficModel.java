package org.ngrinder.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.annotations.Expose;
import com.huawei.argus.serializer.TimestampDatetimeSerializer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;


@SuppressWarnings({"JpaDataSourceORMInspection"})
@Entity
@Table(name = "TRAFFIC_MODEL")
public class TrafficModel extends BaseEntity<TrafficModel> {

	private static final int MAX_LONG_STRING_SIZE = 9990;

	private static final int MAX_STRING_SIZE = 2048;

	public TrafficModel() {

	}

	@Expose
	@Cloneable
	@Column(name = "start_time")
	@JsonSerialize(using = TimestampDatetimeSerializer.class)
	private Date startTime;

	@Expose
	@Cloneable
	@Column(name = "end_time")
	@JsonSerialize(using = TimestampDatetimeSerializer.class)
	private Date endTime;

	@Expose
	@Cloneable
	@Column(name = "regions", length = MAX_STRING_SIZE)
	private String regions;

	@Expose
	@Cloneable
	@Column(name = "perf_method")
	private Integer perfMethod;

	@Expose
	@Cloneable
	@Column(name = "traffic_multiple")
	private Double trafficMultiple;

	@Expose
	@Cloneable
	@Column(name = "peak_choose")
	private Integer peakChoose;

	@Expose
	@Cloneable
	@Column(name = "traffic_peak")
	@JsonSerialize(using = TimestampDatetimeSerializer.class)
	private Date trafficPeak;

	@Expose
	@Cloneable
	@Column(name = "traffic_host")
	private String trafficHost;

	@Expose
	@Cloneable
	@Column(name = "traffic_proportion", columnDefinition = "MEDIUMTEXT")
	private String trafficProportion;

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getRegions() {
		return regions;
	}

	public void setRegions(String regions) {
		this.regions = regions;
	}

	public Integer getPerfMethod() {
		return perfMethod;
	}

	public void setPerfMethod(Integer perfMethod) {
		this.perfMethod = perfMethod;
	}

	public Double getTrafficMultiple() {
		return trafficMultiple;
	}

	public void setTrafficMultiple(Double traffic_multiple) {
		this.trafficMultiple = traffic_multiple;
	}

	public Integer getPeakChoose() {
		return peakChoose;
	}

	public void setPeakChoose(Integer peakChoose) {
		this.peakChoose = peakChoose;
	}

	public Date getTrafficPeak() {
		return trafficPeak;
	}

	public void setTrafficPeak(Date trafficPeak) {
		this.trafficPeak = trafficPeak;
	}

	public String getTrafficHost() {
		return trafficHost;
	}

	public void setTrafficHost(String trafficHost) {
		this.trafficHost = trafficHost;
	}

	public String getTrafficProportion() {
		return trafficProportion;
	}

	public void setTrafficProportion(String trafficProportion) {
		this.trafficProportion = trafficProportion;
	}
}

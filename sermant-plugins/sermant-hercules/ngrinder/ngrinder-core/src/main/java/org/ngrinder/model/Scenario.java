package org.ngrinder.model;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "SCENARIO")
public class Scenario extends BaseEntity<Scenario> {
	private static final int MAX_LONG_STRING_SIZE = 2048;
	private static final long serialVersionUID = 4763026823473478790L;

	@Expose
	@Column(name = "app_name")
	private String appName;

	@Expose
	@Column(name = "scenario_name")
	private String scenarioName;

	@Expose
	@Column(name = "scenario_type")
	private String scenarioType;

	@Expose
	@Column(name = "tag_string")
	private String label;

	@Expose
	@Column(name = "script_path")
	private String scriptPath;

	@Expose
	@Cloneable
	@Column(name = "description", length = MAX_LONG_STRING_SIZE)
	private String desc;

	@ManyToOne
	@JoinColumn(name = "created_user", insertable = true, updatable = false)
	@org.hibernate.annotations.Index(name = "created_user_index")
	@NotFound(action = NotFoundAction.IGNORE)
	private User createBy;

	@Expose
	@Column(name = "create_time")
	private Date createTime;

	@Expose
	@Column(name = "update_time")
	private Date updateTime;

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getScenarioName() {
		return scenarioName;
	}

	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	public String getScenarioType() {
		return scenarioType;
	}

	public void setScenarioType(String scenarioType) {
		this.scenarioType = scenarioType;
	}

	public User getCreateBy() {
		return createBy;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getScriptPath() {
		return scriptPath;
	}

	public void setScriptPath(String scriptPath) {
		this.scriptPath = scriptPath;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setCreateBy(User createBy) {
		this.createBy = createBy;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}


package org.ngrinder.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.annotations.Expose;
import com.huawei.argus.serializer.TimestampDatetimeSerializer;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "TEST_REPORT")
public class TestReport extends BaseEntity<TestReport> {

	private static final long serialVersionUID = -610416022146336477L;

	public TestReport() {

	}

	/**
	 * Constructor.
	 *
	 * @param createdUser crested user.
	 */
	public TestReport(User createdUser) {
		this.setCreatedUser(createdUser);
	}

	@ManyToOne
	@JoinColumn(name = "created_user", insertable = true, updatable = false)
	@org.hibernate.annotations.Index(name = "created_user_index")
	@NotFound(action = NotFoundAction.IGNORE)
	private User createdUser;

	@Expose
	@Cloneable
	@Column(name = "name")
	private String testName;

	@Expose
	@Cloneable
	@Column(name = "test_type")
	private String testType;

	@Expose
	/** the start time of this test. */
	@Column(name = "start_time")
	@JsonSerialize(using = TimestampDatetimeSerializer.class)
	private Date startTime;

	@Expose
	/** the finish time of this test. */
	@Column(name = "finish_time")
	@JsonSerialize(using = TimestampDatetimeSerializer.class)
	private Date finishTime;

	@Expose
	@Column(name = "run_time")
	private String runTime;

	@Expose
	@Column(name = "perf_test_id")
	private Long perfTestId;

	public User getCreatedUser() {
		return createdUser;
	}

	public void setCreatedUser(User createdUser) {
		this.createdUser = createdUser;
	}

	public String getTestName() {
		return testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public String getTestType() {
		return testType;
	}

	public void setTestType(String testType) {
		this.testType = testType;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	public String getRunTime() {
		return runTime;
	}

	public void setRunTime(String runTime) {
		this.runTime = runTime;
	}

	public Long getPerfTestId() {
		return perfTestId;
	}

	public void setPerfTestId(Long perfTestId) {
		this.perfTestId = perfTestId;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}

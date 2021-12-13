package org.ngrinder.model;

import com.google.gson.annotations.Expose;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "SCENARIO_PERF_TEST")
public class ScenarioPerfTest extends BaseEntity<ScenarioPerfTest> {
	private static final long serialVersionUID = -677045547860305004L;

	@Expose
	@Column(name = "scenario_id")
	private Long scenarioId;

	@Expose
	@Column(name = "perf_test_id")
	private Long perfTestId;

	public Long getScenarioId() {
		return scenarioId;
	}

	public void setScenarioId(Long scenarioId) {
		this.scenarioId = scenarioId;
	}

	public Long getPerfTestId() {
		return perfTestId;
	}

	public void setPerfTestId(Long perfTestId) {
		this.perfTestId = perfTestId;
	}
}

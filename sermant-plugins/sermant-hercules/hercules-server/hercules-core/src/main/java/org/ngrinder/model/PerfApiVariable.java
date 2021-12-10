package org.ngrinder.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.annotations.Expose;
import com.huawei.argus.serializer.PerfSceneApiIdSerializer;

import javax.persistence.*;


@SuppressWarnings({"JpaDataSourceORMInspection"})
@Entity
@Table(name = "PERF_API_VARIABLE")
public class PerfApiVariable extends BaseEntity<PerfApiVariable> {

	private static final int MAX_LONG_STRING_SIZE = 9990;

	private static final int MAX_STRING_SIZE = 2048;

	public PerfApiVariable() {

	}

	@Expose
	@Cloneable
	@Column(name = "hearder_name")
	private String headerName;


	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "scene_api_id")
	@JsonSerialize(using = PerfSceneApiIdSerializer.class)
//	@JsonIgnore
	private PerfSceneApi perfSceneApi;

	@Expose
	@Cloneable
	@Column(name = "var_name")
	private String varName;

	@Expose
	@Cloneable
	@Column(name = "match_num")
	private Integer matchNum;

	@Expose
	@Cloneable
	@Column(name = "source")
	private Integer source;

	@Expose
	@Cloneable
	@Column(name = "get_value_method")
	private Integer getValueMethod;

	@Expose
	@Cloneable
	@Column(name = "expression")
	private String expression;

	public PerfSceneApi getPerfSceneApi() {
		return perfSceneApi;
	}

	public void setPerfSceneApi(PerfSceneApi perfSceneApi) {
		this.perfSceneApi = perfSceneApi;
	}

	public String getVarName() {
		return varName;
	}

	public void setVarName(String varName) {
		this.varName = varName;
	}

	public Integer getSource() {
		return source;
	}

	public void setSource(Integer source) {
		this.source = source;
	}

	public Integer getGetValueMethod() {
		return getValueMethod;
	}

	public void setGetValueMethod(Integer getValueMethod) {
		this.getValueMethod = getValueMethod;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getHeaderName() {
		return headerName;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public Integer getMatchNum() {
		return matchNum;
	}

	public void setMatchNum(Integer matchNum) {
		this.matchNum = matchNum;
	}
}

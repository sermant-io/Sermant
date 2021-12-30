package org.ngrinder.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.annotations.Expose;
import com.huawei.argus.serializer.PerfSceneIdSerializer;
import org.ngrinder.common.util.JsonStringAttributeConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;


@SuppressWarnings({"JpaDataSourceORMInspection"})
@Entity
@Table(name = "PERF_SCENE_API")
public class PerfSceneApi extends BaseEntity<PerfSceneApi>{

	private static final int MAX_STRING_SIZE = 2048;

	public PerfSceneApi() {

	}

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "scene_id")
	@JsonSerialize(using = PerfSceneIdSerializer.class)
	private PerfScene perfScene;

	@Expose
	@Cloneable
	@Column(name = "api_name")
	private String apiName;

	@Expose
	@Cloneable
	@Column(name = "url")
	private String url;


	@Expose
	@Cloneable
	@Column(name = "method")
	private String method;

	@Expose
	@Cloneable
	@Column(name = "protocol")
	private String protocol;

	@Expose
	@Cloneable
	@Column(name = "turn")
	private Integer turn;

	@Expose
	@Cloneable
	@Column(name = "time_limit")
	private Integer timeLimit;

	@Expose
	@Cloneable
	@Column(name = "request_body_type", length = 1)
	private Integer requestBodyType;

	@Expose
	@Cloneable
	@Column(name = "raw_body_type", length = 1)
	private Integer rawBodyType;

	@Expose
	@Cloneable
	@Column(name = "request_body_text", length = MAX_STRING_SIZE)
	private String requestBodyStr;

	@Expose
	@Cloneable
	@Column(name = "content_type", length = 1)
	private Integer contentType;
//
//	@Expose
//	@Cloneable
//	@Column(name = "request_header", length = MAX_STRING_SIZE)
//	private String requestHeader;

	@OneToMany(mappedBy = "perfSceneApi", cascade=CascadeType.ALL)
	private List<PerfApiVariable> perfApiVariables = new LinkedList<PerfApiVariable>();

	@OneToMany(mappedBy = "perfSceneApi", cascade=CascadeType.ALL)
	private List<PerfApiCheckPoint> perfApiCheckPoints = new LinkedList<PerfApiCheckPoint>();

	@Column(name = "request_body_keyvalue", length = MAX_STRING_SIZE)
	@Convert(converter = HeadersConverter.class)
	private ArrayList<KeyValue>  requestBodyMap;

	@Column(name = "request_header", length = MAX_STRING_SIZE)
	@Convert(converter = HeadersConverter.class)
	private ArrayList<KeyValue> requestHeader;

	public PerfScene getPerfScene() {
		return perfScene;
	}

	public void setPerfScene(PerfScene perfScene) {
		this.perfScene = perfScene;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public Integer getTurn() {
		return turn;
	}

	public void setTurn(Integer turn) {
		this.turn = turn;
	}

	public Integer getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(Integer timeLimit) {
		this.timeLimit = timeLimit;
	}
	public void setPerfApiVariables(List<PerfApiVariable> perfApiVariables) {
		if(perfApiVariables != null){
			for (PerfApiVariable perfSceneApiV : perfApiVariables) {
				perfSceneApiV.setPerfSceneApi(this);
			}
		}
		this.perfApiVariables = perfApiVariables;
	}

	public String getRequestBodyStr() {
		return requestBodyStr;
	}

	public void setRequestBodyStr(String requestBodyStr) {
		this.requestBodyStr = requestBodyStr;
	}

	public List<PerfApiVariable> getPerfApiVariables() {
		return perfApiVariables;
	}

	public ArrayList<KeyValue> getRequestHeader() {
		return requestHeader;
	}

	public void setRequestHeader(ArrayList<KeyValue> requestHeader) {
		this.requestHeader = requestHeader;
	}

	public List<PerfApiCheckPoint> getPerfApiCheckPoints() {
		return perfApiCheckPoints;
	}

	public void setPerfApiCheckPoints(List<PerfApiCheckPoint> perfApiCheckPoints) {
		if(perfApiCheckPoints != null){
			for (PerfApiCheckPoint perfSceneApiC : perfApiCheckPoints) {
				perfSceneApiC.setPerfSceneApi(this);
			}
		}
		this.perfApiCheckPoints = perfApiCheckPoints;
	}
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ArrayList<KeyValue> getRequestBodyMap() {
		return requestBodyMap;
	}

	public void setRequestBodyMap(ArrayList<KeyValue> requestBodyMap) {
		this.requestBodyMap = requestBodyMap;
	}

	public Integer getRequestBodyType() {
		return requestBodyType;
	}

	public void setRequestBodyType(Integer requestBodyType) {
		this.requestBodyType = requestBodyType;
	}

	public Integer getRawBodyType() {
		return rawBodyType;
	}

	public void setRawBodyType(Integer rawBodyType) {
		this.rawBodyType = rawBodyType;
	}

	public Integer getContentType() {
		return contentType;
	}

	public void setContentType(Integer contentType) {
		this.contentType = contentType;
	}

	static class KeyValue implements Serializable {
		private String key;
		private String value;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	private static class HeadersConverter extends JsonStringAttributeConverter<ArrayList<KeyValue>> {}

}

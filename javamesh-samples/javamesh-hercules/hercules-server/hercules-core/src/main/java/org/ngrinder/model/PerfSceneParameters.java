package org.ngrinder.model;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.google.gson.annotations.Expose;
import com.sun.net.httpserver.Headers;
import org.ngrinder.common.util.JsonStringAttributeConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"JpaDataSourceORMInspection"})
@Entity
@Table(name = "PERF_SCENE_PARAMETERS")
public class PerfSceneParameters extends BaseEntity<PerfSceneParameters>{
	public PerfSceneParameters(){

	}

	@Column(name = "customize_parameters")
	@Convert(converter = CustomizeParametersConverter.class)
	private ArrayList<CustomizeParameter> customizeParameters;

	@Column(name = "global_headers")
	@Convert(converter = HeadersConverter.class)
	private ArrayList<Header> globalHeaders;


	public List<CustomizeParameter> getCustomizeParameters() {
		return customizeParameters;
	}

	public void setCustomizeParameters(ArrayList<CustomizeParameter> customizeParameters) {
		this.customizeParameters = customizeParameters;
	}

	public ArrayList<Header> getGlobalHeaders() {
		return globalHeaders;
	}

	public void setGlobalHeaders(ArrayList<Header> globalHeaders) {
		this.globalHeaders = globalHeaders;
	}



	private static class HeadersConverter extends JsonStringAttributeConverter<ArrayList<CustomizeParameter>>{}

	private static class CustomizeParametersConverter extends JsonStringAttributeConverter<ArrayList<CustomizeParameter>>{}

	static class Header implements Serializable {
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

	static class CustomizeParameter extends Header {
	}
}

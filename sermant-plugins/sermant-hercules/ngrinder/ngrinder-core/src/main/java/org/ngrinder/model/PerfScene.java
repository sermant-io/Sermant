package org.ngrinder.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.huawei.argus.serializer.PerfSceneType;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


@SuppressWarnings({"JpaDataSourceORMInspection"})
@Entity
@Table(name = "PERF_SCENE")
public class PerfScene extends MyBaseModel<PerfScene> {

	private static final int MAX_LONG_STRING_SIZE = 2048;

	private static final long serialVersionUID = -1;

	private static final int MAX_STRING_SIZE = 2048;

	public PerfScene() {

	}

	/**
	 * Constructor.
	 *
	 * @param createdUser crested user.
	 */
	public PerfScene(User createdUser) {
		this.setCreatedUser(createdUser);
	}

	/**
	 * 根据压测场景类型获取压测场景对应的脚本路径
	 * @return
	 */
	@JsonIgnore
	public String getPerfSceneScriptPath() {
		if(this.getType().equals(PerfSceneType.FLOW)) {
//			return this.getSceneName() + "_" + this.getId() + "/" + this.getSceneName() + "/src/main/java/TestRunner.groovy";
			return  this.getId() + "/src/main/java/TestRunner.groovy";
		} else if (this.getType().equals(PerfSceneType.TRAFFIC)) {
			return "_SCENE_TRAFFIC/" + this.getId() + ".groovy";
		} else {
			return this.getScriptPath();
		}
	}

	/**
	 * 根据压测场景类型返回场景对应脚本文件夹路径
	 * @return
	 */
	@JsonIgnore
	public String getPerfSceneFolderPath() {
		if(this.getType().equals(PerfSceneType.FLOW)) {
			return  this.getId()+"";
		} else {
			return getPerfSceneScriptPath();
		}
	}

	@Expose
	@Cloneable
	@Column(name = "scene_name")
	private String sceneName;

	@Expose
	@Cloneable
	@Column(name = "description")
	private String description;

	@Expose
	@Cloneable
	@Column(name = "scene_type")
	@Enumerated(EnumType.ORDINAL)
	private PerfSceneType type;

	@Expose
	@Cloneable
	@Column(name = "script_path")
	private String scriptPath;

	@Expose
	@Cloneable
	@Column(name = "vuser_sum")
	private Integer vuserSum;			// 需要的虚拟用户数

	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "traffic_choose_id", referencedColumnName = "id")
	private TrafficChoose trafficChoose;

	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name = "traffic_model_id", referencedColumnName = "id")
	private TrafficModel trafficModel;

	@OneToMany(mappedBy = "perfScene", cascade=CascadeType.ALL)
	@OrderColumn(name = "turn")
	private List<PerfSceneApi> perfSceneApis = new LinkedList<PerfSceneApi>();

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "global_parameters_id", referencedColumnName = "id")
	private PerfSceneParameters globalParameters;

	public String getSceneName() {
		return sceneName;
	}

	public void setSceneName(String sceneName) {
		this.sceneName = sceneName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public PerfSceneType getType() {
		if (type == null)
			return PerfSceneType.UNKNOWN;
		return type;
	}

	public void setType(PerfSceneType type) {
		this.type = type;
	}

	public String getScriptPath() {
		return scriptPath;
	}

	public void setScriptPath(String scriptPath) {
		this.scriptPath = scriptPath;
	}

	public Integer getVuserSum() {
		return vuserSum;
	}

	public void setVuserSum(Integer vuserSum) {
		this.vuserSum = vuserSum;
	}

	public TrafficChoose getTrafficChoose() {
		return trafficChoose;
	}

	public void setTrafficChoose(TrafficChoose trafficChoose) {
		this.trafficChoose = trafficChoose;
	}

	public TrafficModel getTrafficModel() {
		return trafficModel;
	}

	public void setTrafficModel(TrafficModel trafficModel) {
		this.trafficModel = trafficModel;
	}

	public List<PerfSceneApi> getPerfSceneApis() {
		return perfSceneApis;
	}

	public void setPerfSceneApis(List<PerfSceneApi> perfSceneApis) {
		if (this.perfSceneApis != null) {
			for (PerfSceneApi perfSceneApi : this.perfSceneApis) {
				perfSceneApi.setPerfScene(null);
			}
		}
		if (perfSceneApis != null) {
			for (PerfSceneApi perfSceneApi : perfSceneApis) {
				perfSceneApi.setPerfScene(this);
			}
		}
		this.perfSceneApis = perfSceneApis;
	}

	public PerfSceneParameters getGlobalParameters() {
		return globalParameters;
	}

	public void setGlobalParameters(PerfSceneParameters globalParameters) {
		this.globalParameters = globalParameters;
	}
}

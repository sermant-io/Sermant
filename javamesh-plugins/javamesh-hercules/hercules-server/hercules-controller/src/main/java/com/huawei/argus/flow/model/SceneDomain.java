package com.huawei.argus.flow.model;

import org.ngrinder.model.PerfScene;
import org.ngrinder.model.PerfSceneApi;

import java.util.List;

/**
 * Created by x00377290 on 2019/4/19.
 */
public class SceneDomain {
    private PerfScene scene;
	private List<PerfSceneApi> apis;

	public List<PerfSceneApi> getApis() {
		return apis;
	}
	public void setApis(List<PerfSceneApi> apis) {
		this.apis = apis;
	}


    public PerfScene getScene() {
        return scene;
    }

    public void setScene(PerfScene scene) {
        this.scene = scene;
    }


}

package com.huawei.argus.flow.service;

import com.huawei.argus.flow.model.SceneDomain;
import org.ngrinder.model.PerfScene;
import org.ngrinder.model.PerfSceneApi;
import org.ngrinder.model.User;
import org.springframework.stereotype.Service;

/**
 * Created by x00377290 on 2019/4/22.
 */
@Service
public interface IPerfSceneAPiService {
	/**
	 * 创建一个流程编排场景
	 * @param sceneDomain 场景实例
	 * @return 生成后的场景实例
	 */
	public PerfScene createScene(User user,PerfScene sceneDomain);

	/**
	 * 更新一个流程编排场景
	 * @param sceneDomain 场景实例
	 * @return 跟新后的场景实例
	 */
	public PerfScene updateScene(User user,long id,PerfScene sceneDomain);

	public PerfSceneApi create(User user,PerfSceneApi perfSceneApi);
}

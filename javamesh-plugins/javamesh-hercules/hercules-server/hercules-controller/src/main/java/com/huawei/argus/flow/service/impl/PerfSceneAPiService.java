package com.huawei.argus.flow.service.impl;

import com.huawei.argus.scene.repository.PerfSceneRepository;
import org.ngrinder.model.*;
import com.huawei.argus.flow.repository.PerfSceneApiRepository;
import com.huawei.argus.flow.service.IPerfSceneAPiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by x00377290 on 2019/4/22.
 */
@Service
public class PerfSceneAPiService implements IPerfSceneAPiService {

	@Autowired
	private PerfSceneRepository sceneRepository;

	@Autowired
	private PerfSceneApiRepository perfSceneApiRepository;

	/**
	 * 创建一个流程编排场景
	 * @param sceneDomain 场景实例
	 * @return 生成后的场景实例
	 */
	@Override
	public PerfScene createScene(User user,PerfScene sceneDomain) {
		sceneDomain.setCreatedUser(user);
		sceneDomain.setLastModifiedUser(user);
		return sceneRepository.save(sceneDomain);
	}

	/**
	 * 更新一个流程编排场景
	 * @param sceneDomain 场景实例
	 * @return 跟新后的场景实例
	 */
	@Override
	public PerfScene updateScene(User user,long id,PerfScene sceneDomain) {
		PerfScene perfSceneForUpdate = sceneRepository.getOne(id);
		perfSceneForUpdate.setSceneName(sceneDomain.getSceneName());
		perfSceneForUpdate.setDescription(sceneDomain.getDescription());
		perfSceneForUpdate.setType(sceneDomain.getType());
		perfSceneForUpdate.setTrafficChoose(sceneDomain.getTrafficChoose());
		perfSceneForUpdate.setTrafficModel(sceneDomain.getTrafficModel());
		perfSceneForUpdate.setGlobalParameters(sceneDomain.getGlobalParameters());
		perfSceneForUpdate.setPerfSceneApis(sceneDomain.getPerfSceneApis());
		perfSceneForUpdate.setLastModifiedUser(user);
		return sceneRepository.save(sceneDomain);
	}

	@Override
	public PerfSceneApi create(User user,PerfSceneApi perfSceneApi) {
		return perfSceneApiRepository.save(perfSceneApi);
	}

//	public PerfSceneApi update(Long id, PerfSceneApi perfSceneApi) {
//		PerfSceneApi perfSceneApiToUpdate = perfSceneApiRepository.getOne(id);
//		perfSceneApiToUpdate.setPerfScene(perfSceneApi.getPerfScene());
//		return perfSceneApiRepository.save(perfSceneApiToUpdate);
//	}
}

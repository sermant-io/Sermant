/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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

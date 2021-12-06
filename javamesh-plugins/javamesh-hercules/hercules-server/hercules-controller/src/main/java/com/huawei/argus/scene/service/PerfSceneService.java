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

package com.huawei.argus.scene.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.argus.perftest.repository.PerfTestTaskRepository;
import com.huawei.argus.scene.repository.PerfSceneRepository;
import com.huawei.argus.serializer.PerfSceneType;
import com.huawei.argus.template.TrafficTemplate;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.http.client.utils.URIBuilder;
import org.bson.Document;
import org.ngrinder.model.*;
import org.ngrinder.script.handler.ScriptHandlerFactory;
import org.ngrinder.script.model.FileEntry;
import org.ngrinder.script.model.FileType;
import org.ngrinder.script.service.FileEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static com.huawei.argus.scene.repository.PerfSceneSpecification.createdBy;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.in;

/**
 * @Author: j00466872
 * @Date: 2019/4/22 15:10
 */
@Service
public class PerfSceneService {
	private static final Logger LOG = LoggerFactory.getLogger(PerfSceneService.class);
	private final Properties mongodbProps = PropertiesLoaderUtils.loadProperties(new ClassPathResource("/mongoDB.properties"));
	public static final String SCRIPT_TYPE = "argus_flow";

	private int trafficIndex = 0;

	@Autowired
	private PerfSceneRepository perfSceneRepository;

	@Autowired
	private PerfTestTaskRepository perfTestTaskRepository;

	@Autowired
	private FileEntryService fileEntryService;

	@Autowired
	private ScriptHandlerFactory scriptHandlerFactory;


	public PerfSceneService() throws IOException {
	}

	/**
	 * 获取用户创建的场景
	 *
	 * @param user
	 * @return
	 */
	public List<PerfScene> list(User user) {
		Specifications<PerfScene> spec = Specifications.where(createdBy(user));
		return perfSceneRepository.findAll(spec);
	}

	public Page<PerfScene> listPaged(User user, Pageable pageable) {
		Specifications<PerfScene> spec = Specifications.where(createdBy(user));
		return perfSceneRepository.findAll(spec, pageable);
	}

	/**
	 * 查询一个场景
	 *
	 * @param id
	 * @return
	 */
	public PerfScene retrieve(Long id) {
		return perfSceneRepository.findOne(id);
	}

	/**
	 * 创建场景
	 *
	 * @param user
	 * @param perfScene
	 * @return
	 */

	/**
	 * 为动态编排场景和引流压测场景保存脚本
	 *
	 * @param user
	 * @param perfScene
	 * @throws IOException
	 */
	private void saveSceneScript(User user, PerfScene perfScene) throws IOException {
		if (perfScene.getType().equals(PerfSceneType.FLOW)) {//FLOW 场景配置生成
			ObjectMapper mapper = new ObjectMapper();
			String sceneJson = mapper.writeValueAsString(perfScene);
			fileEntryService.prepareNewEntryForFlowTest(user, sceneJson, perfScene.getPerfSceneFolderPath(), "",
				scriptHandlerFactory.getHandler(SCRIPT_TYPE), scriptHandlerFactory.getHandler(SCRIPT_TYPE).getClass());
			LOG.info("success create script to " + perfScene.getSceneName());

			perfScene.setScriptPath(perfScene.getPerfSceneScriptPath());
		} else if (perfScene.getType().equals(PerfSceneType.TRAFFIC)) {
			List<Object> urlsAll = new ArrayList();
			List<String> savedApiList = new ArrayList<>();


			FindIterable<Document> trafficQuerySet = queryTrafficSet(perfScene.getTrafficModel(), perfScene.getTrafficChoose());
			trafficIndex = 0;

			trafficQuerySet.forEach(new Block<Document>() {
				@Override
				public void apply(final Document document) {
					Map<String, Object> urlInfo = new HashMap<>();
					try {
						URIBuilder uriBuilder = new URIBuilder(document.get("traffic_url").toString());
						String methodApi = document.get("http_method") + " " + uriBuilder.getPath();
						if (!savedApiList.contains(methodApi)) {
							savedApiList.add(methodApi);
							if (document.get("http_method").equals("PUT") || document.get("http_method").equals("POST")) {
								urlInfo.put("api", methodApi);
								urlInfo.put("count", 1);
								urlInfo.put("traffic_url", document.get("traffic_url"));
								urlInfo.put("http_method", document.get("http_method"));
								urlInfo.put("token", document.get("token"));
								urlInfo.put("request_body", new ArrayList<String>(Arrays.asList(document.get("request_body").toString())));
							} else {
								urlInfo.put("api", methodApi);
								urlInfo.put("count", 1);
								urlInfo.put("traffic_url", document.get("traffic_url"));
								urlInfo.put("http_method", document.get("http_method"));
								urlInfo.put("token", document.get("token"));
							}
							urlsAll.add(urlInfo);
						} else {
							int n = savedApiList.indexOf(methodApi);
							urlInfo = (Map<String, Object>) urlsAll.get(n);
							urlInfo.put("count", (int) urlInfo.get("count") + 1);
							if (document.get("http_method").equals("PUT") || document.get("http_method").equals("POST")) {
								ArrayList<String> requestBodyList = (ArrayList) urlInfo.get("request_body");
								requestBodyList.add(document.get("request_body").toString());
								urlInfo.put("request_body", requestBodyList);
							}
						}

						trafficIndex += 1;
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}

//					if (document)
					System.out.println(document.toJson());
				}
			});

			TrafficModel trafficModel = perfScene.getTrafficModel();
			// 如果有traffic_proportion，可以生成脚本
			if (trafficModel != null) {
				// 流量倍数处理
				for (Object urlInfo : urlsAll) {
					HashMap<String, Object> urlInfoMap = (HashMap<String, Object>) urlInfo;
					urlInfoMap.put("count", (int) ((int) urlInfoMap.get("count") * trafficModel.getTrafficMultiple()));
					urlInfo = urlInfoMap;
				}
				// TODO 处理单独修改API的流量条数
				ObjectMapper mapper = new ObjectMapper();
				List<HashMap> trafficProportion = mapper.readValue(trafficModel.getTrafficProportion(), List.class);
				for (HashMap trafficInfo : trafficProportion) {
					HashMap<String, Object> trafficInfoMap = (HashMap<String, Object>) trafficInfo;
				}
				// 计算API数量
				int apiSum = urlsAll.size();
				String trafficProportionFull = mapper.writeValueAsString(urlsAll);
				String scriptValue = String.format(TrafficTemplate.VALUE, "'" + trafficProportionFull + "'",
					"'" + trafficModel.getTrafficHost() + "'", apiSum);
				FileEntry scriptFile = new FileEntry();
				scriptFile.setFileType(FileType.GROOVY_SCRIPT);
				scriptFile.setPath(perfScene.getPerfSceneScriptPath());
				scriptFile.setContent(scriptValue);
				scriptFile.setDescription("");

				fileEntryService.save(user, scriptFile);

				perfScene.setScriptPath(scriptFile.getPath());
			}
		}
	}

	@Transactional(rollbackOn = Exception.class)
	public PerfScene create(User user, PerfScene perfScene) throws Exception {
		perfScene.setCreatedUser(user);
		perfScene.setLastModifiedUser(user);
		PerfScene perfSceneReturn = perfSceneRepository.save(perfScene);

		// 保存脚本
		this.saveSceneScript(user, perfSceneReturn);
		return perfSceneReturn;
	}

	/**
	 * 更新场景
	 *
	 * @param perfScene
	 * @return
	 */
	@Transactional(rollbackOn = Exception.class)
	public PerfScene update(User user, Long id, PerfScene perfScene) throws Exception {
		PerfScene perfSceneToUpdate = perfSceneRepository.getOne(id);
		PerfScene perfSceneToDelete = perfSceneToUpdate.cloneTo(new PerfScene());
		// update properties
		perfSceneToUpdate.setSceneName(perfScene.getSceneName());
		perfSceneToUpdate.setDescription(perfScene.getDescription());
		perfSceneToUpdate.setType(perfScene.getType());
		perfSceneToUpdate.setTrafficChoose(perfScene.getTrafficChoose());
		perfSceneToUpdate.setTrafficModel(perfScene.getTrafficModel());
		perfSceneToUpdate.setGlobalParameters(perfScene.getGlobalParameters());
		perfSceneToUpdate.setPerfSceneApis(perfScene.getPerfSceneApis());
		PerfScene perfSceneReturn = perfSceneRepository.save(perfSceneToUpdate);
		perfSceneToUpdate.setLastModifiedUser(user);
		// 更新脚本
		this.saveSceneScript(user, perfSceneToUpdate);
		return perfSceneReturn;
	}

	/**
	 * 删除场景
	 *
	 * @param id
	 */
	public void delete(User user, Long id) {
		PerfScene perfSceneToDelete = perfSceneRepository.getOne(id);
		// 找出所有关联该场景的任务，取消场景关联
		List<PerfTest> perfTestList = perfTestTaskRepository.getPerfTestsByPerfScene(perfSceneToDelete);

		for (PerfTest perfTest : perfTestList) {
			perfTest.setPerfScene(null);
		}

		// 先删除相应脚本
		if (perfSceneToDelete.getType().equals(PerfSceneType.TRAFFIC))
			fileEntryService.delete(user, perfSceneToDelete.getPerfSceneFolderPath());
		else if (perfSceneToDelete.getType().equals(PerfSceneType.FLOW))
			this.deleteScriptFlow(user, perfSceneToDelete);

		perfSceneRepository.delete(id);
	}

	/**
	 * @param user
	 * @param perfSceneToDelete
	 */
	private void deleteScriptFlow(User user, PerfScene perfSceneToDelete) {
		String filesString = perfSceneToDelete.getPerfSceneFolderPath();

		String[] files = filesString.split(",");
		fileEntryService.delete(user, "", files);
	}


	public List<PerfScene> getTaskDataByUser(User user) {
//		id,scene_name,scene_type,vuser_sum,script_path
		if(user.getRole().equals(Role.ADMIN)){
			return perfSceneRepository.findAll();
		}
		return perfSceneRepository.getTaskDataByUser(user.getId());
	}

	private MongoCollection<Document> getTrafficDocument() {
		MongoClient client = new MongoClient(mongodbProps.getProperty("mongo.host"), Integer.parseInt(mongodbProps.getProperty("mongo.port")));
		MongoDatabase clientDatabase = client.getDatabase(mongodbProps.getProperty("mongo.database"));
		return clientDatabase.getCollection(mongodbProps.getProperty("mongo.trafficTable"));
	}

	Block<Document> printBlock = new Block<Document>() {
		@Override
		public void apply(final Document document) {
			System.out.println(document.toJson());
		}
	};

	public FindIterable<Document> queryTrafficSet(TrafficModel trafficModel, TrafficChoose trafficChoose) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		List<Integer> regionList = mapper.readValue(trafficModel.getRegions(), List.class);
		List<Integer> executeIdList = mapper.readValue(trafficChoose.getExecuteIds(), List.class);

		return this.getTrafficDocument().find(and(
//			lt("start_time", trafficModel.getEndTime()),
//			gt("start_time", trafficModel.getStartTime()),
			in("record_task_execute_id", executeIdList),
			in("traffic_log_region", regionList)));
//		this.getTrafficDocument().find(in("record_task_execute_id", executeIdList)).forEach(printBlock);
//		return null;
	}
}

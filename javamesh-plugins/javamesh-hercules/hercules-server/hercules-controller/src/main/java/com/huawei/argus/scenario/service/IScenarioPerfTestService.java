package com.huawei.argus.scenario.service;

import org.ngrinder.model.ScenarioPerfTest;
import org.ngrinder.model.User;

import java.util.List;

public interface IScenarioPerfTestService {
	/**
	 * 保存压测场景与压测任务关系
	 *
	 * @param scenarioPerfTest
	 * @return
	 */
	ScenarioPerfTest save(ScenarioPerfTest scenarioPerfTest);

	/**
	 * 根据压测任务ID或压测场景ID查询
	 * @param user
	 * @param perfTestId
	 * @param scenarioId
	 * @return
	 */
	List<ScenarioPerfTest> getAllByID(User user, Long perfTestId, Long scenarioId);

	/**
	 * 根据场景ID查找压测任务关联信息
	 * @param user
	 * @param scenarioIds
	 * @return
	 */
	List<ScenarioPerfTest> getAllByScenarioIds(User user, Long[] scenarioIds);

	/**
	 * 删除:三个ID，有且只有一个有值时使用此接口
	 * @param user
	 * @param id
	 * @param perfTestId
	 * @param scenarioId
	 */
	void deleteByOneId(User user, Long id, Long perfTestId, Long scenarioId);
}

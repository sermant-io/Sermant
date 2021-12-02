package com.huawei.argus.perftest.service;

import org.ngrinder.model.PerfTest;
import org.ngrinder.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.util.List;

public interface IPerfTestTaskService {
	/**
	 * 分页查询PerfTest集合
	 * @param user
	 * @param query
	 * @param tag
	 * @param queryFilter
	 * @param pageable
	 * @return
	 */
	public Page<PerfTest> getPagedAll(User user, String query, String tag, String queryFilter, Pageable pageable);

	/**
	 * 多id删除（多个id之间英文逗号分隔）
	 * @param user
	 * @param ids
	 */
	public void deletePerfTests(User user, String ids);

	/**
	 * 根据唯一id查询
	 * @param testId
	 * @return
	 */
	public PerfTest getOne(Long testId);

	File getPerfTestDirectory(PerfTest perfTest);

	PerfTest getOneWithTag(Long id);

	String getProcessAndThreadPolicyScript();

	/**
	 * 添加、修改之后的保存
	 * @param user
	 * @param perfTest
	 * @return
	 */
	PerfTest save(User user, PerfTest perfTest);

	PerfTest getPerfTestTask(User user, Long id, boolean b);

	PerfTest startPerfTest(User user, PerfTest id);

	void stopPerfTests(User user, String ids);

	List<String> getSelectAgentNameList(PerfTest perfTest);

	public List<String> getLogFiles(Long id);
}

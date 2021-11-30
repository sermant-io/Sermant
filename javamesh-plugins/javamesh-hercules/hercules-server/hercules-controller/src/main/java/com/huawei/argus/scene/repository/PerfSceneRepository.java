package com.huawei.argus.scene.repository;

import org.ngrinder.model.PerfScene;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * {@link PerfScene} Repository.
 *
 * @Author: j00466872
 * @Date: 2019/4/22 15:09
 */
public interface PerfSceneRepository extends JpaRepository<PerfScene, Long>, JpaSpecificationExecutor<PerfScene> {

	Page<PerfScene> findAll(Specification<PerfScene> spec, Pageable pageable);


	@Query(value = "SELECT * FROM `PERF_SCENE` WHERE created_user = ?1",nativeQuery = true)
	List<PerfScene> getTaskDataByUser(Long id);
}

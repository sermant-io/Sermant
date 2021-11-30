package com.huawei.argus.flow.repository;

import org.ngrinder.model.PerfSceneApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

/**
 * Created by x00377290 on 2019/4/22.
 */
public interface PerfSceneApiRepository  extends JpaRepository<PerfSceneApi, Long>, JpaSpecificationExecutor<PerfSceneApi> {
}

package com.huawei.argus.scenario.repository;

import org.ngrinder.model.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ScenarioRepository extends JpaRepository<Scenario, Long>, JpaSpecificationExecutor<Scenario> {
}

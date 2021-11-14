package com.huawei.argus.flow.service;

import com.huawei.argus.flow.repository.PerfSceneParametersRepository;
import org.ngrinder.model.PerfSceneParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PerfSceneParametersService {
	@Autowired
	PerfSceneParametersRepository perfSceneParametersRepository;

	public PerfSceneParameters create(PerfSceneParameters perfSceneParameters){
		return perfSceneParametersRepository.save(perfSceneParameters);
	}
}

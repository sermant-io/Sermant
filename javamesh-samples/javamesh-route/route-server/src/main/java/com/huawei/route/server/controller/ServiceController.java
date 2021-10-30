package com.huawei.route.server.controller;

import com.huawei.route.server.controller.entity.ResponseService;
import com.huawei.route.server.controller.entity.ServiceRequest;
import com.huawei.route.server.entity.AbstractInstance;
import com.huawei.route.server.entity.AbstractService;
import com.huawei.route.server.repository.ServiceInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 对外接口服务，提供agent访问接口
 *
 * @param <S> 服务类型
 * @param <T> 实例类型
 * @author zhouss
 * @since 2021-10-09
 */
@RestController
@RequestMapping(value = "/route/v1")
public class ServiceController<S extends AbstractService<T>, T extends AbstractInstance> {
    @Autowired
    private ServiceInstanceRepository<S, T> serviceInstanceRepository;

    /**
     * 查询服务实例列表
     *
     * @param request 请求参数
     * @return 实例列表
     */
    @PostMapping("/instance/condition/list")
    public List<ResponseService<T>> queryServiceInstances(@RequestBody ServiceRequest request) {
        return serviceInstanceRepository.queryServiceInstance(request.getServiceNames(), request.getLdc(), request.getTagName());
    }

}

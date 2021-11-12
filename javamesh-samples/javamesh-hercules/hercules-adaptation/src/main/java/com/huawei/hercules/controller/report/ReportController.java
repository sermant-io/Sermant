package com.huawei.hercules.controller.report;

import com.huawei.hercules.controller.BaseController;
import com.huawei.hercules.service.perftest.IPerfTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ReportController extends BaseController {

    @Autowired
    IPerfTestService perftestService;


}

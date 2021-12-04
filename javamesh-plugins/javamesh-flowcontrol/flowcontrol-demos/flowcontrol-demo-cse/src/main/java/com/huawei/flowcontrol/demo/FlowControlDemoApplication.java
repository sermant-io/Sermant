/**
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package com.huawei.flowcontrol.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流控能力测试
 *
 * @author zhouss
 * @since 2021-11-29
 */
@SpringBootApplication
@RestController
public class FlowControlDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlowControlDemoApplication.class);
    }

    @GetMapping("/flow")
    public String flow() {
        return "I am flow";
    }

    @GetMapping("/degrade")
    public String degrade() throws InterruptedException {
        Thread.sleep(101);
        return "I am degrader";
    }
}

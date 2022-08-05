/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.intergration.common.rule;

import com.huaweicloud.intergration.common.CommonConstants;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Optional;

/**
 * 测试规则
 *
 * @author zhouss
 * @since 2022-08-02
 */
public abstract class AbstractTestRule implements TestRule {
    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                final String type = System.getProperty(CommonConstants.TEST_TYPE);
                if (type == null) {
                    base.evaluate();
                    return;
                }
                final Optional<SermantTestType> of = SermantTestType.of(type);
                if (!of.isPresent()) {
                    return;
                }
                if (of.get() == SermantTestType.ALL || isSupport(of.get())) {
                    base.evaluate();
                }
            }
        };
    }

    /**
     * 是否支持
     *
     * @param testType 测试类型
     * @return 是否支持
     */
    protected abstract boolean isSupport(SermantTestType testType);
}

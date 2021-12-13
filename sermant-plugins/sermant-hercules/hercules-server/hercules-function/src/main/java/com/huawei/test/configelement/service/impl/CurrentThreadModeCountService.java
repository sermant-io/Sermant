/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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
 */

package com.huawei.test.configelement.service.impl;

import com.huawei.test.configelement.service.BaseGrinderCountService;
import com.huawei.test.configelement.service.ExecuteTimesInfo;

/**
 * 功能描述：Current Thread模式下的取数逻辑实现,即取数仅仅根据当前线程的情况来计算，不受其他线程干扰
 *
 * @author zl
 * @since 2021-12-16
 */
public class CurrentThreadModeCountService extends BaseGrinderCountService {
	@Override
	protected int doIncrement(ExecuteTimesInfo executeTimesInfo) {
		// 直接返回执行次数就是当前线程的取数值
		return executeTimesInfo.getRunNumber();
	}
}

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

package com.huawei.test.preprocessor;

import com.huawei.test.BasePressureTestFunction;

/**
 * 功能描述：初始化URL修改器
 *
 * @author zl
 * @since 2021-12-09
 */
public abstract class URLModifier<T> extends BasePressureTestFunction {
	/**
	 * 修改url
	 *
	 * @return 修改之后的url
	 */
	public abstract String modifyUrl(String url, String sessionId, T config);
}

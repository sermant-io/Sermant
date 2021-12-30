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

package com.huawei.test.configelement;

import java.util.Map;

/**
 * 功能描述：参数化接口
 *
 * @author zl
 * @since 2021-12-20
 */
public interface IParameterized {
	/**
	 * 是否还有下一个数据，这里可能会根据recycleOnEof参数影响，如果打开这个，就会一直返回true
	 *
	 * @return true：有，false：没有
	 */
	public boolean hasNext();

	/**
	 * 当有下一个数据时，返回下一行所有的数据，这个值在参数化文件中的位置是经过参数化中sharingMode来决定的
	 *
	 * @return 根据SharingMode计算出来的下一行的所有参数化取值
	 */
	public Map<String, String> nextLineValue();
}

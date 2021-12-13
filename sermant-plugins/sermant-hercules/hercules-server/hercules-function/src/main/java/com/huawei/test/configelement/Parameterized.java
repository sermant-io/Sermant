/*
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
 */

package com.huawei.test.configelement;

import com.huawei.test.configelement.config.ParameterizedConfig;

/**
 * 功能描述：参数化数据源配置
 *
 * @author zl
 * @since 2021-12-08
 */
public abstract class Parameterized extends ConfigElement<ParameterizedConfig> {
	/**
	 * 是否还有下一个数据，这里可能会根据recycleOnEof参数影响，如果打开这个，就会一直返回true
	 *
	 * @return true：有，false：没有
	 */
	public abstract boolean hasNext();

	/**
	 * 当有下一个数据时，返回下一行所有的数据，这个值是经过参数化中sharingMode来决定的
	 *
	 * @return 根据SharingMode计算出来的下一行的所有参数化取值
	 */
	public abstract String[] nextLineValue();

	/**
	 * 当有下一个数据时，返回下一行数据中某一个参数key对应的值，这个值是经过参数化中sharingMode来决定的
	 *
	 * @param paramKey 参数key
	 * @return 根据SharingMode计算出来的下一行的某一个参数key对应的值
	 */
	public abstract String nextSpecifyValue(String paramKey);

	/**
	 * 当有下一个数据时，返回下一行数据中某一个value的index对应的值，这个值是经过参数化中sharingMode来决定的
	 *
	 * @param valueIndex value的index
	 * @return 根据SharingMode计算出来的下一行的某一个参数valueIndex对应的值
	 */
	public abstract String nextSpecifyValue(int valueIndex);

	/**
	 * 获取根据参数化SharingMode获取所有分配的值
	 *
	 * @return 根据参数化SharingMode获取所有分配的值
	 */
	public abstract String[][] obtainAllValues();
}

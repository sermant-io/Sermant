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

package com.huawei.javamesh.core.lubanops.core.transfer;

import java.io.IOException;
import java.util.List;

import com.huawei.javamesh.core.lubanops.core.common.ConnectionException;
import com.huawei.javamesh.core.lubanops.integration.access.Address;
import com.huawei.javamesh.core.lubanops.integration.access.MessageWrapper;

/**
 * @author
 * @date 2020/10/29 20:08
 */
public interface InvokerService {

    void setAccessAddressList(List<Address> accessAddressList);

    void sendDataReport(MessageWrapper message) throws ConnectionException, IOException;

    boolean isSendEnable();

    void setNeedConnect(boolean needConnect);

}

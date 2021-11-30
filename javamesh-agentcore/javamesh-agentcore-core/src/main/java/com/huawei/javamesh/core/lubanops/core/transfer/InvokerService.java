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

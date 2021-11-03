package com.huawei.apm.core.lubanops.core.transfer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections4.ListUtils;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.huawei.apm.core.lubanops.bootstrap.config.AgentConfigManager;
import com.huawei.apm.core.lubanops.bootstrap.config.ConfigManager;
import com.huawei.apm.core.lubanops.bootstrap.event.SecureChangeEvent;
import com.huawei.apm.core.lubanops.bootstrap.exception.ApmRuntimeException;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.apm.core.lubanops.core.common.ConnectionException;

import com.huawei.apm.core.lubanops.integration.access.Address;
import com.huawei.apm.core.lubanops.integration.access.MessageWrapper;
import com.huawei.apm.core.lubanops.integration.enums.AddressScope;
import com.huawei.apm.core.lubanops.integration.enums.Protocol;
import com.huawei.apm.core.lubanops.integration.transport.websocket.LubanWebSocketClient;

/**
 * Data transfer invoker.
 * @author
 */
@Singleton
public class TransferInvokerService extends AbstractInvokerService {
    private final static Logger LOG = LogFactory.getLogger();

    private List<Address> innerAddressList = new ArrayList<Address>();

    private List<Address> outerAddressList = new ArrayList<Address>();

    private volatile boolean needReconnect = false;

    @Inject
    EventBus eventBus;

    @Override
    public void setAccessAddressList(List<Address> accessAddressList) {
        String configAddress = AgentConfigManager.getAccessAddress();
        if (!StringUtils.isBlank(configAddress)) {
            accessAddressList = getAddressListFromConfig(configAddress, accessAddressList);
        }
        List<Address> tmpInnerAddressList = new ArrayList<Address>();
        List<Address> tmpOuterAddressList = new ArrayList<Address>();

        for (Address address : accessAddressList) {
            if (AddressScope.inner.equals(address.getScope())) {
                tmpInnerAddressList.add(address);
            } else if (AddressScope.outer.equals(address.getScope())) {
                tmpOuterAddressList.add(address);
            }
        }
        if (!ListUtils.isEqualList(innerAddressList, tmpInnerAddressList)) {
            this.innerAddressList = tmpInnerAddressList;
            needReconnect = true;
        }
        if (!ListUtils.isEqualList(outerAddressList, tmpOuterAddressList)) {
            this.outerAddressList = tmpOuterAddressList;
            needReconnect = true;
        }
    }

    private List<Address> getAddressListFromConfig(String configAddress, List<Address> accessAddressList) {
        List<Address> result = new ArrayList<Address>();
        try {
            String[] addressArgs = configAddress.split(",");
            for (String addressArg : addressArgs) {
                Address address = new Address();
                String[] addr = addressArg.split(":");
                address.setHost(addr[0]);
                int port = Integer.valueOf(addr[1]);
                address.setPort(port);
                address.setSport(port);
                address.setProtocol(Protocol.WS);
                address.setScope(AddressScope.inner);
                result.add(address);
            }
            return result;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "解析配置文件中access地址错误", e);
        }
        return accessAddressList;
    }

    @Override
    protected LubanWebSocketClient createConnectConfig() throws ConnectionException {

        if (innerAddressList == null && outerAddressList == null) {
            throw new RuntimeException("access地址没有下发");
        }
        IOException ex = null;
        // 优先连内网
        try {
            if (innerAddressList != null && innerAddressList.size() > 0) {
                LubanWebSocketClient connectConfig = connectTransfer(ex, innerAddressList);

                if (connectConfig != null) {
                    return connectConfig;
                } else {
                    LOG.log(Level.SEVERE,
                            "[TRANSFER INVOKER]failed to connect to monitor innerAddressList:" + innerAddressList);
                }
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE,
                    "failed to connect to monitor innerAddressList:" + innerAddressList + ",exception:" + e.getClass()
                            .getName(),
                    e);
            ex = e;
        }
        // TODO move to client
        try {
            if (outerAddressList != null && outerAddressList.size() > 0) {
                LubanWebSocketClient connectConfig = connectTransfer(ex, outerAddressList);
                if (connectConfig != null) {
                    return connectConfig;
                } else {
                    LOG.log(Level.SEVERE,
                            "[TRANSFER INVOKER]failed to connect to monitor outerAddressList:" + outerAddressList);
                }
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE,
                    "failed to connect to monitor outerAddressList:" + outerAddressList + ",exception:" + e.getClass()
                            .getName(),
                    e);
            ex = e;
        }
        throw new ConnectionException("failed to connect to monitor list", ex);
    }

    private LubanWebSocketClient connectTransfer(IOException ex, List<Address> addressList) throws IOException {
        if (AgentConfigManager.getProxyList().length > 0) {
            List<String> proxyIpList = Arrays.asList(AgentConfigManager.getProxyList());
            Collections.shuffle(proxyIpList);
            for (String proxy : proxyIpList) {
                LubanWebSocketClient client = connectProxy(addressList, ConfigManager.isSecureChannel(), proxy);
                // 连接成功之后就无需重连
                needReconnect = false;
                return client;
            }
        } else {
            LubanWebSocketClient config = connect(addressList, ConfigManager.isSecureChannel());
            // 连接成功之后就无需重连
            needReconnect = false;
            return config;
        }
        return null;
    }

    /**
     * 底层重试两次,防止由于socket断掉,数据发送失败
     * @return
     * @throws IOException
     * @throws ConnectionException
     * @throws Exception
     */
    @Override
    public void sendDataReport(MessageWrapper message) throws ConnectionException, IOException {
        super.sendRequest(message);
    }

    @Override
    protected boolean needReconnect() {

        return needReconnect;
    }

    @Override
    public void init() throws ApmRuntimeException {

    }

    /**
     * reset reconnect flag on secure changed.
     * @param event
     */
    @Subscribe
    public void onSecureChange(SecureChangeEvent event) {
        LOG.log(Level.INFO,
                String.format("[TRANSFER INVOKER]access transfer type changed,event[{%s}]", event.toString()));
        setNeedConnect(true);
    }

    @Override
    public void setNeedConnect(boolean needConnect) {
        this.needReconnect = needConnect;
    }
}

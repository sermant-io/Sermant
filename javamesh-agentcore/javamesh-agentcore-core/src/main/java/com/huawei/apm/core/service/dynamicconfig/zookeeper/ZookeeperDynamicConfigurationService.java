package com.huawei.apm.core.service.dynamicconfig.zookeeper;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.service.dynamicconfig.Config;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigurationService;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Zookeeper implementation for DynamicConfigurationService
 *
 */
public class ZookeeperDynamicConfigurationService implements DynamicConfigurationService {

    private static final Logger logger = LogFactory.getLogger();

    ZooKeeper zkClient;

    static private ZookeeperDynamicConfigurationService serviceInst;

    private ZookeeperDynamicConfigurationService()
    {

    }

    @Override
    public String getDefaultGroup() {
        return Config.getDefaultGroup();
    }

    @Override
    public long getDefaultTimeout() {
        return Config.getTimeout_value();
    }

    public static synchronized  ZookeeperDynamicConfigurationService getInstance()
    {
        if ( serviceInst == null )
        {
            serviceInst = new ZookeeperDynamicConfigurationService();
            URI zk_uri;

            try {
                zk_uri = new URI(Config.getZookeeperUri());
            } catch (URISyntaxException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e);
            }

            String zk_con_str = zk_uri.getHost();
            if ( zk_uri.getPort() > 0 )
            {
                zk_con_str = zk_con_str + ":" + zk_uri.getPort();
            }

            ZooKeeper zkInst;
            try {
                zkInst = new ZooKeeper(zk_con_str, Config.getTimeout_value(), new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {}
                });
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e);
            }

            serviceInst.zkClient = zkInst;
        }

        return serviceInst;
    }


    @Override
    public boolean addListener(String key, String group, ConfigurationListener listener) {

        if (listener == null)
            return false;

        if (group == null)
            group = "";
        else
            group = "/" + group;

        Stat st = new Stat();
        String finalGroup = group;
        Watcher wc = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                String content = getConfig(event.getPath());
                ConfigChangedEvent cce = new ConfigChangedEvent(key, finalGroup, content);
                if ( ! event.getPath().equals(finalGroup + key) )
                    logger.log(Level.WARNING, "unexpected event " + event.toString() + " for " + key + ":" + finalGroup);
                listener.process(cce);
            }
        };

        try {
            zkClient.getData(group + key, wc, st) ;
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * @param key      postfix of key path
     * @param group    prefix of key path
     * @param listener configuration listener
     */
    @Override
    public boolean removeListener(String key, String group, ConfigurationListener listener) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param key      key path
     */
    @Override
    public String getConfig(String key, String group)  {

        if (group == null)
            group = "";
        else
            group = "/" + group;

        String rs = null;
        try {
            Stat st = new Stat();
            rs = new String(zkClient.getData(group + key, false, st));
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return null;
        }
        return rs;
    }


    @Override
    public boolean publishConfig(String key, String group, String content) {

        if (group == null)
            group = "";
        else
            group = "/" + group;

        boolean rs = false;
        try {
            rs = this.updateNode(group + key, content.getBytes(StandardCharsets.UTF_8), -1);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
        return rs;
    }



    protected boolean createRecursivly(String path)
    {
        try {
            System.out.println(path);
            if (zkClient.exists(path, null) == null && path.length() > 0) {
                String temp = path.substring(0, path.lastIndexOf("/"));
                if ( temp != null && temp.length() > 1 ) {
                    System.out.println(temp);
                    createRecursivly(temp);
                }
                zkClient.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }else{

            }
        } catch (KeeperException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return false;
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
        return true;

    }

    protected boolean updateNode(String path, byte[] data, int version)
    {
        try {
            if ( zkClient.exists(path, null) == null ) {
                createRecursivly(path);
            }
            zkClient.setData(path, data, version);
        } catch (KeeperException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return false;
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
        return true;

    }

    @Override
    public void start()
    {
        if ( ZookeeperDynamicConfigurationService.getInstance() == null )
        {
            logger.log(Level.SEVERE, "init zookeeper config error");
            throw new RuntimeException("init zookeeper config error");
        }
    }

    @Override
    public void stop() {

        try {
            zkClient.close();
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }

    }
}

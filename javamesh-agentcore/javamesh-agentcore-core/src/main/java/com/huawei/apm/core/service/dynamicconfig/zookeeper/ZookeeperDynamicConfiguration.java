package com.huawei.apm.core.service.dynamicconfig.zookeeper;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.service.dynamicconfig.ConfigChangedEvent;
import com.huawei.apm.core.service.dynamicconfig.ConfigurationListener;
import com.huawei.apm.core.service.dynamicconfig.DynamicConfiguration;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZookeeperDynamicConfiguration implements DynamicConfiguration {

    private static final Logger logger = LogFactory.getLogger();

    ZooKeeper zkClient;

    String group;

    public ZookeeperDynamicConfiguration(ZooKeeper zkClient, String group) {
        this.zkClient = zkClient;
        this.group = group;
    }

    @Override
    public String getDefaultGroup() {
        return this.group;
    }

    @Override
    public void addListener(String key, String group, ConfigurationListener listener) {
        Stat st = new Stat();
        Watcher wc = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                String content = getConfig(event.getPath());
                ConfigChangedEvent cce = new ConfigChangedEvent(key, group, content);
                if ( ! event.getPath().equals(group+key) )
                    logger.log(Level.WARNING, "unexpected event " + event.toString() + " for " + key + ":" + group);
                listener.process(cce);
            }
        };

        try {
            zkClient.getData("/" + group + key, wc, st) ;
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    /**
     * @param key      postfix of key path
     * @param group    prefix of key path
     * @param listener configuration listener
     */
    @Override
    public void removeListener(String key, String group, ConfigurationListener listener) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param key      key path
     */
    @Override
    public String getConfig(String key, String group)  {

        String rs = null;
        try {
            Stat st = new Stat();
            rs = new String(zkClient.getData("/" + group + key, false, st));
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return null;
        }
        return rs;
    }


    @Override
    public boolean publishConfig(String key, String group, String content) {
        boolean rs = false;
        try {
            rs = this.updateNode("/" + group + key, content.getBytes(StandardCharsets.UTF_8), -1);
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
}

package com.huawei.apm.core.service.dynamicconfig.zookeeper;

import com.huawei.apm.core.common.LoggerFactory;
import com.huawei.apm.core.service.dynamicconfig.Config;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangeType;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigurationService;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Zookeeper implementation for DynamicConfigurationService
 *
 */
public class ZookeeperDynamicConfigurationService implements DynamicConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger();

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

    private String getPath(String key, String group) {
        group = fixGroup(group);
        return group.startsWith("/") ? group + key : '/' + group + key;
    }

    private String fixGroup(String group) {
        return group == null ? getDefaultGroup() : group;
    }

    private ConfigChangeType transEventType(Watcher.Event.EventType type) {
        switch (type) {
            case NodeCreated:
                return ConfigChangeType.ADDED;
            case NodeDeleted:
                return ConfigChangeType.DELETED;
            case None:
            case NodeDataChanged:
            case DataWatchRemoved:
            case ChildWatchRemoved:
            case NodeChildrenChanged:
            case PersistentWatchRemoved:
            default:
                return ConfigChangeType.MODIFIED;
        }
    }


    @Override
    public boolean addConfigListener(String key, String group, ConfigurationListener listener) {

        if (listener == null)
            return false;

        final String finalGroup = fixGroup(group);
        final String fullPath = getPath(key, finalGroup);
        Watcher wc = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if ( ! fullPath.equals(event.getPath()) )
                    logger.log(Level.WARNING, "unexpected event " + event + " for " + key + ":" + finalGroup);
                String content = getConfig(key, finalGroup);
                ConfigChangeType changeType = transEventType(event.getType());
                ConfigChangedEvent cce = new ConfigChangedEvent(key, finalGroup, content, changeType);
                listener.process(cce);
            }
        };

        try {
            zkClient.addWatch(fullPath, wc, AddWatchMode.PERSISTENT) ;
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
    public boolean removeConfigListener(String key, String group, ConfigurationListener listener) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param key      key path
     */
    @Override
    public String getConfig(String key, String group)  {

        final String fullPath = getPath(key, group);

        String rs = null;
        try {
            Stat st = new Stat();
            rs = new String(zkClient.getData(fullPath, false, st));
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return null;
        }
        return rs;
    }


    @Override
    public boolean publishConfig(String key, String group, String content) {

        final String fullPath = getPath(key, group);

        boolean rs = false;
        try {
            rs = this.updateNode(fullPath, content.getBytes(StandardCharsets.UTF_8), -1);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
        return rs;
    }



    protected boolean createRecursivly(String path)
    {
        try {
            if (zkClient.exists(path, null) == null && path.length() > 0) {
                String temp = path.substring(0, path.lastIndexOf("/"));
                if ( temp != null && temp.length() > 1 ) {
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
    public void close()
    {
        try {
            this.zkClient.close();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public List<String> listConfigsFromGroup(String group)
    {
        group = group.trim();
        if (group.startsWith("/") == false)
        {
            group = "/"+group;
        }
        List<String> str_array = null;
        try {
            str_array = listNodesFromNode(group);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        return str_array;
    }

    @Override
    public List<String> listConfigsFromConfig(String key, String group)
    {
        group = group.trim();
        if (group.startsWith("/") == false)
        {
            group = "/"+group;
        }
        key = key.trim();
        if (key.startsWith("/") == false)
        {
            key = "/"+key;
        }

        List<String> str_array = null;
        try {
            str_array = listNodesFromNode(group+key);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        return str_array;
    }

    private List<String> listNodesFromNode(String node)
    {
        List<String> str_array = new Vector<String>();
        try {
            str_array = zkClient.getChildren(node, null);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        for (int i = 0; i < str_array.size(); i++)
        {
            String str = str_array.get(i);
            for (String grandChild : listNodesFromNode(node + "/" + str)) {
                str_array.add(str + '/' + grandChild);
            }
        }

        return str_array;
    }


}

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

package com.huawei.javamesh.core.service.dynamicconfig.zookeeper;

import com.huawei.javamesh.core.common.LoggerFactory;
import com.huawei.javamesh.core.service.dynamicconfig.Config;
import com.huawei.javamesh.core.service.dynamicconfig.service.ConfigChangeType;
import com.huawei.javamesh.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.javamesh.core.service.dynamicconfig.service.ConfigurationListener;
import com.huawei.javamesh.core.service.dynamicconfig.service.DynamicConfigurationService;
import com.huawei.javamesh.core.service.dynamicconfig.utils.LabelGroupUtils;
import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Zookeeper implementation for DynamicConfigurationService
 */
public class ZookeeperDynamicConfigurationService implements DynamicConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger();

    ZooKeeper zkClient;

    static private ZookeeperDynamicConfigurationService serviceInst;

    private ZookeeperDynamicConfigurationService() {

    }

    @Override
    public String getDefaultGroup() {
        return Config.getDefaultGroup();
    }

    @Override
    public long getDefaultTimeout() {
        return Config.getTimeout_value();
    }

    public static synchronized ZookeeperDynamicConfigurationService getInstance() {
        if (serviceInst == null) {
            serviceInst = new ZookeeperDynamicConfigurationService();
            URI zk_uri;

            try {
                zk_uri = new URI(Config.getZookeeperUri());
            } catch (URISyntaxException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e);
            }

            String zk_con_str = zk_uri.getHost();
            if (zk_uri.getPort() > 0) {
                zk_con_str = zk_con_str + ":" + zk_uri.getPort();
            }

            ZooKeeper zkInst;
            try {
                zkInst = new ZooKeeper(zk_con_str, Config.getTimeout_value(), new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                    }
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

    /**
     * 添加组监听
     * 若由Kie配置中心转换而来，则配置路径为<h4>/group/key</h4>
     * <pre>
     * 其中:
     * group: 由{@link LabelGroupUtils#createLabelGroup(Map)}生成
     * key: 则是对应kie的键名
     * </pre>
     * <p>第一次添加会将group下的所有子路径的数据通知给监听器</p>
     *
     * @param group    分组
     * @param listener 监听器
     * @return boolean
     */
    @Override
    public boolean addGroupListener(String group, ConfigurationListener listener) {
        try {
            if (listener == null) {
                return false;
            }
            // 监听group底下所有的子节点数据变更
            final String path = getPath("", fixGroup(group));
            zkClient.addWatch(path, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getPath() == null || event.getPath().equals(path)
                            || !event.getPath().startsWith(path)) {
                        return;
                    }
                    // 带有分隔符"/"的键
                    String keyWithSeparator = event.getPath().substring(group.length() + 1);
                    if (keyWithSeparator.length() < 1) {
                        return;
                    }
                    final String content = getConfig(keyWithSeparator, group);
                    listener.process(new ConfigChangedEvent(keyWithSeparator.substring(1), group, content,
                            transEventType(event.getType())));
                }
            }, AddWatchMode.PERSISTENT_RECURSIVE);
            notifyGroup(group, listener);
        } catch (KeeperException.NoNodeException ignored) {
            // ignored
        } catch (Exception e) {
            logger.log(Level.WARNING,
                    String.format(Locale.ENGLISH, "Added zookeeper group listener failed, %s", e.getMessage()), e);
            return false;
        }
        return true;
    }

    @Override
    public boolean removeGroupListener(String key, String group, ConfigurationListener listener) {
        return true;
    }

    /**
     * 添加zookeeper路径监听
     * 将会监听路径<code>/group/key</code>的数据变更
     * <h3>一次添加将会将节点数据通知给监听器</h3>
     *
     * @param key 子路径
     * @param group 父路径
     * @param listener 监听器
     * @return 当连接zk失败返回false
     */
    @Override
    public boolean addConfigListener(String key, String group, ConfigurationListener listener) {

        if (listener == null)
            return false;

        final String finalGroup = fixGroup(group);
        final String fullPath = getPath(key, finalGroup);
        Watcher wc = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (!fullPath.equals(event.getPath()))
                    logger.log(Level.WARNING, "unexpected event " + event + " for " + key + ":" + finalGroup);
                String content = getConfig(key, finalGroup);
                ConfigChangeType changeType = transEventType(event.getType());
                ConfigChangedEvent cce = new ConfigChangedEvent(key, finalGroup, content, changeType);
                listener.process(cce);
            }
        };

        try {
            zkClient.addWatch(fullPath, wc, AddWatchMode.PERSISTENT);
            notifyKey(key, group, listener);
        } catch (KeeperException.NoNodeException ignored) {
            // ignored
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
     * @param key key path
     */
    @Override
    public String getConfig(String key, String group) {

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


    protected boolean createRecursivly(String path) {
        try {
            if (zkClient.exists(path, null) == null && path.length() > 0) {
                String temp = path.substring(0, path.lastIndexOf("/"));
                if (temp != null && temp.length() > 1) {
                    createRecursivly(temp);
                }
                zkClient.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else {

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

    protected boolean updateNode(String path, byte[] data, int version) {
        try {
            if (zkClient.exists(path, null) == null) {
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
    public void close() {
        try {
            this.zkClient.close();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Override
    public List<String> listConfigsFromGroup(String group) {
        group = group.trim();
        if (group.startsWith("/") == false) {
            group = "/" + group;
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
    public List<String> listConfigsFromConfig(String key, String group) {
        group = group.trim();
        if (group.startsWith("/") == false) {
            group = "/" + group;
        }
        key = key.trim();
        if (key.startsWith("/") == false) {
            key = "/" + key;
        }

        List<String> str_array = null;
        try {
            str_array = listNodesFromNode(group + key);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        return str_array;
    }

    private List<String> listNodesFromNode(String node) {
        List<String> str_array = new Vector<String>();
        try {
            if (zkClient.exists(node, false) != null) {
                str_array = zkClient.getChildren(node, null);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        for (int i = 0; i < str_array.size(); i++) {
            String str = str_array.get(i);
            for (String grandChild : listNodesFromNode(node + "/" + str)) {
                str_array.add(str + '/' + grandChild);
            }
        }

        return str_array;
    }

    /**
     * 第一次增加监听器时，将关联查询的数据传给listener
     *
     * @param group    分组
     * @param listener 监听器
     */
    private void notifyGroup(String group, ConfigurationListener listener) {
        final List<String> keys = listConfigsFromGroup(group);
        if (keys != null) {
            for (String key : keys) {
                notifyKey(key, group, listener);
            }
        }
    }

    private void notifyKey(String key, String group, ConfigurationListener listener) {
        final String content = getConfig(fixKey(key), group);
        listener.process(new ConfigChangedEvent(key, group, content, ConfigChangeType.ADDED));
    }

    private String fixKey(String key) {
        if (key == null) {
            return null;
        }
        return key.startsWith("/") ? key : "/" + key;
    }
}

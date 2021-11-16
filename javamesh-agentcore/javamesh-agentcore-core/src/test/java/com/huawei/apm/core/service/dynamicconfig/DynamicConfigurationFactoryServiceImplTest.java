package com.huawei.apm.core.service.dynamicconfig;

import com.huawei.apm.core.config.ConfigLoader;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.service.CoreService;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigurationFactoryService;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigurationService;
import com.huawei.apm.core.service.dynamicconfig.zookeeper.ZookeeperDynamicConfigurationService;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

public class DynamicConfigurationFactoryServiceImplTest {

//    ZooKeeper zkClient;
    URI uri;
    String zpath = "/";


    @Before
    public void setUp() {

        try {
            uri = new URI("zookeeper://127.0.0.1:2181");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Assert.fail();
        }
        Logger logger = Logger.getLogger("test");
        LogFactory.setLogger(logger);

    }

    @Test
    public void testZKClient() {
        ZooKeeper zkClient = null;

        try {
            //System.out.println(uri.getRawPath());
            //System.out.println(uri.getPath());
            zkClient = new ZooKeeper(uri.getHost() + ":" + uri.getPort(), 30000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

        if (zkClient != null) {
            try {
                List<String> zooChildren = new ArrayList<String>();
                zooChildren = zkClient.getChildren(zpath, false);
                System.out.println("Znodes of '/': ");
                for (String child: zooChildren) {
                    //print the children
                    System.out.println(child);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail();
            }
        } else {
            System.err.println("zkClient is null");
            Assert.fail();
        }
    }

    @Test
    public void testFactory() {

        ServiceLoader<CoreService> sl = ServiceLoader.load(CoreService.class);
        DynamicConfigurationFactoryService dcfs = null;

        for ( CoreService cs : sl)
        {
            if (cs.getClass().toString().contains("DynamicConfigurationFactoryServiceImpl"))
            {
                dcfs = (DynamicConfigurationFactoryService) cs;
                break;
            }
        }
        Assert.assertTrue(dcfs != null);

        DynamicConfigurationService dcs = dcfs.getDynamicConfigurationService();
        dcs.publishConfig("/test", "test", "test");
        String rs = dcs.getConfig("/test", "test");
        Assert.assertTrue(rs.equals("test"));

    }

    @Test
    public void testService() {

        ZookeeperDynamicConfigurationService zdcs = null;
        try {
            zdcs = ZookeeperDynamicConfigurationService.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

        DynamicConfigurationService dcs = zdcs;

        dcs.publishConfig("/test", "test", "test");
        String rs = dcs.getConfig("/test", "test");
        Assert.assertTrue(rs.equals("test"));

        zdcs.publishConfig("/test/test11", "test2", "test22");
        rs = zdcs.getConfig("/test/test11", "test2");
        Assert.assertTrue(rs.equals("test22"));

        zdcs.addListener("/test/test11", new ConfigurationListener() {
            @Override
            public void process(ConfigChangedEvent event) {
                System.out.println(event.toString());
            }
        });
        zdcs.publishConfig("/test/test11", "test3", "test22");
    }



}

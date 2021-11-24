package com.huawei.apm.core.service.dynamicconfig;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.service.BaseService;
import com.huawei.apm.core.service.dynamicconfig.service.*;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigurationFactoryService;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigurationService;
import com.huawei.apm.core.service.dynamicconfig.zookeeper.ZookeeperDynamicConfigurationService;
import org.apache.zookeeper.KeeperException;
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
    public void testConfig() {
        DynamicConfigType type = DynamicConfigType.valueOf("NOP");
        try {
            type = DynamicConfigType.valueOf("Nop");
            Assert.fail("Should not comes here");
        } catch ( IllegalArgumentException e ) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testZKClient() {
        ZooKeeper zkClient = null;

        try {
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
        try {
            List<String> str_array = zkClient.getChildren("/test2", null);
            System.out.println(str_array);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFactory() {

        ServiceLoader<BaseService> sl = ServiceLoader.load(BaseService.class);
        DynamicConfigurationFactoryService dcfs = null;

        for ( BaseService cs : sl)
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

        zdcs.addConfigListener("/test/test11", "test2", new ConfigurationListener() {
            @Override
            public void process(ConfigChangedEvent event) {

                System.out.println(event.toString());

            }
        });
        zdcs.publishConfig("/test/test11", "test3", "test22");
        List<String> configs = zdcs.listConfigsFromGroup("test3");

        System.out.println("end");
    }



}

package zookeeper;

import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import com.huawei.apm.core.dynamicconfig.ConfigChangedEvent;
import com.huawei.apm.core.dynamicconfig.ConfigurationListener;
import com.huawei.apm.core.dynamicconfig.DynamicConfiguration;
import com.huawei.apm.core.dynamicconfig.zookeeper.ZookeeperDynamicConfigurationFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

public class ZooKeeperDynamicConfigurationFactoryTest {

//    ZooKeeper zkClient;
    URI uri;
    String zpath = "/";


    @Before
    public void setUp() {

        try {
            uri = new URI("zookeeper://localhost:2181");
        } catch (URISyntaxException e) {
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
            System.out.println(uri.getRawSchemeSpecificPart());
            zkClient = new ZooKeeper(uri.getHost() + ":" + uri.getPort(), 30000, null);
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

        ZookeeperDynamicConfigurationFactory zdcf = null;
        try {
            zdcf = (ZookeeperDynamicConfigurationFactory) Class.forName("com.huawei.apm.core.dynamicconfig.zookeeper.ZookeeperDynamicConfigurationFactory").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

        DynamicConfiguration dc = zdcf.getDynamicConfiguration(uri);

        dc.publishConfig("/test", "test", "test");
        String rs = dc.getConfig("/test", "test");
        Assert.assertTrue(rs.equals("test"));



        dc.publishConfig("/test/test11", "test2", "test22");
        rs = dc.getConfig("/test/test11", "test2");
        Assert.assertTrue(rs.equals("test22"));

        dc.addListener("/test/test11", new ConfigurationListener() {
            @Override
            public void process(ConfigChangedEvent event) {
                System.out.println(event.toString());
            }
        });
        dc.publishConfig("/test/test11", "test3", "test22");
    }



}

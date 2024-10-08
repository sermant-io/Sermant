package io.sermant.premain.common;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * BootArgsBuilder Unit Test
 *
 * @author lilai
 * @since 2022-11-26
 */
public class BootArgsBuilderTest {
    private static final Map<String, String> envMap = new HashMap<>();

    /**
     * Set environment variables before unit tests
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @BeforeClass
    public static void setUp() throws NoSuchFieldException, IllegalAccessException {
        envMap.put("serviceAName", "demo");
        envMap.put("serviceAVersion", "1.0");
        envMap.put("serviceBName", "");
        envMap.put("serviceBVersion", "");
        setEnv(envMap);
    }

    /**
     * Clean up environment variables after unit tests
     *
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @AfterClass
    public static void tearDown() throws NoSuchFieldException, IllegalAccessException {
        envMap.put("serviceAName", "");
        envMap.put("serviceAVersion", "");
        setEnv(envMap);
    }

    /**
     * Test the getActualValue method
     *
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    @Test
    public void testGetActualValue()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Method method = BootArgsBuilder.class.getDeclaredMethod("getActualValue", String.class);
        method.setAccessible(true);
        assertEquals("demo", method.invoke(BootArgsBuilder.class, "${serviceAName:A}"));
        assertEquals("1.0", method.invoke(BootArgsBuilder.class, "${serviceAVersion:2.0}"));
        assertEquals("B", method.invoke(BootArgsBuilder.class, "${serviceBName:B}"));
        assertEquals("2.0", method.invoke(BootArgsBuilder.class, "${serviceBVersion:2.0}"));
    }

    /**
     * Set environment variables
     *
     * @param envMap
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static void setEnv(Map envMap) throws NoSuchFieldException, IllegalAccessException {
        try {
            // win
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map env1 = (Map) theEnvironmentField.get(null);
            env1.putAll(envMap);

            // linux/macos
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField(
                    "theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map env2 = (Map) theCaseInsensitiveEnvironmentField.get(null);
            env2.putAll(envMap);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map env = System.getenv();
            for (Class cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map map = (Map) obj;
                    map.clear();
                    map.putAll(envMap);
                }
            }
        }
    }

    /**
     * Test parse augments
     */
    @Test
    public void testParseArgs() {
        String agentArgs = "appName=test,command=INSTALL_PLUGIN:monitor/flowcontrol,server.port=9000";
        Map<String, String> agentArgsMap = AgentArgsResolver.resolveAgentArgs(agentArgs);
        Map<String, Object> bootArgsMap = new HashMap<>(agentArgsMap);
        BootArgsBuilder.build(bootArgsMap, PathDeclarer.getAgentPath());
        Assert.assertEquals("test", bootArgsMap.get("appName"));
        Assert.assertEquals("9000", bootArgsMap.get("server.port"));
        Assert.assertEquals("INSTALL_PLUGIN:monitor/flowcontrol", bootArgsMap.get("command"));
    }
}

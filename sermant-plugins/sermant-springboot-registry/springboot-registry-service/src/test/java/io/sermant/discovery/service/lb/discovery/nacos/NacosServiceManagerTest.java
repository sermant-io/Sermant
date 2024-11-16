package io.sermant.discovery.service.lb.discovery.nacos;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NacosServiceManagerTest {

    private NacosServiceManager nacosServiceManagerInstance;

    @Before
    public void setUp() {
        // Obtain an instance of NacosServiceManager before running tests
        nacosServiceManagerInstance = NacosServiceManager.getInstance();
    }

    @Test
    public void testGetInstance_ShouldReturnSameInstance() {
        // Act
        NacosServiceManager anotherInstance = NacosServiceManager.getInstance();

        // Assert
        assertNotNull("Instance should not be null", nacosServiceManagerInstance);
        assertNotNull("Another instance should not be null", anotherInstance);
        assertEquals("Both instances should be the same", nacosServiceManagerInstance, anotherInstance);
    }

    @Test
    public void testGetInstance_ShouldBeThreadSafe() {
        // Arrange
        final int threadCount = 100;
        Thread[] threads = new Thread[threadCount];
        NacosServiceManager[] instances = new NacosServiceManager[threadCount];

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                instances[finalI] = NacosServiceManager.getInstance();
            });
        }

        // Act
        for (Thread thread : threads) {
            thread.start();
        }

        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Assert
        for (int i = 0; i < threadCount; i++) {
            assertNotNull("Instance should not be null", instances[i]);
            assertEquals("All instances should be the same", instances[0], instances[i]);
        }
    }
}

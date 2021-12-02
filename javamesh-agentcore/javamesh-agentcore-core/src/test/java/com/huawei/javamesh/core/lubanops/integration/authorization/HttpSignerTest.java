package com.huawei.javamesh.core.lubanops.integration.authorization;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.huawei.javamesh.core.lubanops.integration.transport.http.HttpSigner;

/**
 * HttpSigner Tester.
 * @author <Authors name>
 * @version 1.0
 * @since
 * 
 *        <pre>
 * 8�� 10, 2020
 *        </pre>
 */
public class HttpSignerTest {

    HttpSigner signer;

    @Before
    public void before() throws Exception {

        signer = new HttpSigner();
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: sign(Request request)
     */
    @Test
    public void testSign() throws Exception {
    }

    /**
     * Method: verify(Request request)
     */
    @Test
    public void testVerify() throws Exception {
        // TODO: Test goes here...
    }

}

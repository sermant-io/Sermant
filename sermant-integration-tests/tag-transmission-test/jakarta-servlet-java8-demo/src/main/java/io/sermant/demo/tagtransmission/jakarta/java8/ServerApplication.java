/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.demo.tagtransmission.jakarta.java8;

import io.sermant.demo.tagtransmission.jakarta.java8.servlet.TestServlet;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

/**
 * simple java8 jakarta servlet demo
 *
 * @since 2025-05-28
 */
public class ServerApplication {
    private static final int PORT = 9050;
    private static final String CONTEXT_PATH = "";

    /**
     * start tomcat
     *
     * @param args boot args
     * @throws LifecycleException if start tomcat failed
     */
    public static void main(String[] args) throws LifecycleException {
        // tomcat instance
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);

        // tomcat connector
        Connector connector = new Connector("HTTP/1.1");
        connector.setPort(PORT);
        tomcat.setConnector(connector);

        // context
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext(CONTEXT_PATH, docBase);

        // servlet
        tomcat.addServlet(CONTEXT_PATH, "testServlet", new TestServlet());
        context.addServletMappingDecoded("/jakarta-servlet/httpServer", "testServlet");

        // start tomcat
        tomcat.start();
        tomcat.getServer().await();
    }
}

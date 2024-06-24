/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.implement.service.xds;

import io.sermant.core.utils.FileUtils;
import io.sermant.implement.service.xds.client.XdsClient;
import io.sermant.implement.service.xds.handler.StreamObserverRequestImpl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * @author daizhenyu
 * @since 2024-05-25
 **/
public abstract class BaseXdsTest {
    private static MockedStatic<FileUtils> mockedFileUtils;

    protected static XdsClient client;

    protected static StreamObserverRequestImpl requestStreamObserver;

    @BeforeClass
    public static void before() {
        requestStreamObserver = new StreamObserverRequestImpl();
        client = Mockito.mock(XdsClient.class);
        Mockito.doReturn(requestStreamObserver).when(client).getDiscoveryRequestObserver(Mockito.any());

        mockedFileUtils = Mockito.mockStatic(FileUtils.class);
        mockedFileUtils.when(() -> FileUtils.readFileToString(Mockito.any())).thenReturn("default");
    }

    @AfterClass
    public static void after() {
        Mockito.clearAllCaches();
        if (mockedFileUtils != null) {
            mockedFileUtils.close();
        }
    }
}

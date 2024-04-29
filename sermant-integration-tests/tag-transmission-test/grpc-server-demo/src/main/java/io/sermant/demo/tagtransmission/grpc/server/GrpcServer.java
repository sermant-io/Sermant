/*
 *  Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.demo.tagtransmission.grpc.server;

import io.sermant.demo.tagtransmission.grpc.server.serviceimpl.GrpcTagTransmissionServiceImpl;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * grpc server端
 *
 * @author daizhenyu
 * @since 2023-10-08
 **/
@Component
public class GrpcServer implements CommandLineRunner {
    @Value("${grpc.server.port}")
    private int grpcPort;

    @Autowired
    private GrpcTagTransmissionServiceImpl tagTransmissionService;

    private Server server;

    @Override
    public void run(String[] args) throws InterruptedException, IOException {
        start();
        blockUntilShutdown();
    }

    private void start() throws IOException {
        server = ServerBuilder.forPort(grpcPort)
                .addService(tagTransmissionService)
                .build()
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            GrpcServer.this.stop();
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
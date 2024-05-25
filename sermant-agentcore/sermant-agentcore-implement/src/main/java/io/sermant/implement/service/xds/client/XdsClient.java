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

package io.sermant.implement.service.xds.client;

import io.envoyproxy.envoy.service.discovery.v3.AggregatedDiscoveryServiceGrpc;
import io.envoyproxy.envoy.service.discovery.v3.AggregatedDiscoveryServiceGrpc.AggregatedDiscoveryServiceStub;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.ConfigManager;
import io.sermant.core.service.xds.config.XdsConfig;
import io.sermant.core.utils.FileUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLException;

/**
 * XdsClient
 *
 * @author daizhenyu
 * @since 2024-05-09
 **/
public class XdsClient implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private XdsConfig config = ConfigManager.getConfig(XdsConfig.class);

    private ManagedChannel channel;

    /**
     * construction method
     */
    public XdsClient() {
        createChannel();
    }

    private void createChannel() {
        NettyChannelBuilder nettyChannelBuilder = NettyChannelBuilder.forTarget(config.getControlPlaneAddress());
        if (config.isSecurityEnable()) {
            try {
                SslContext context = GrpcSslContexts.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
                nettyChannelBuilder.sslContext(context);
            } catch (SSLException e) {
                LOGGER.log(Level.SEVERE, "SSLException occurred when creating gRPC channel", e);
            }
        } else {
            nettyChannelBuilder.usePlaintext();
        }
        channel = nettyChannelBuilder.build();
    }

    /**
     * update channel
     */
    public void updateChannel() {
        if (channel == null || channel.isShutdown() || channel.isTerminated()) {
            synchronized (this) {
                if (channel == null || channel.isShutdown() || channel.isTerminated()) {
                    createChannel();
                }
            }
        }
    }

    /**
     * get ADS grpc request stream observer
     *
     * @param observer DiscoveryResponse observer
     * @return StreamObserver
     */
    public StreamObserver<DiscoveryRequest> getDiscoveryRequestObserver(StreamObserver<DiscoveryResponse> observer) {
        AggregatedDiscoveryServiceStub stub = AggregatedDiscoveryServiceGrpc.newStub(channel);
        if (config.isSecurityEnable()) {
            Metadata header = new Metadata();
            Key<String> authorization = Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
            header.put(authorization, "Bearer " + FileUtils.readFileToString(config.getTokenPath()));
            stub = MetadataUtils.attachHeaders(stub, header);
        }
        return stub.streamAggregatedResources(observer);
    }

    @Override
    public void close() throws IOException {
        if (channel != null) {
            channel.shutdown();
        }
    }
}

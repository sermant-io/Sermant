/*
 *   Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.demo.tagtransmission.grpc.client.controller;

import io.sermant.demo.tagtransmission.grpc.api.service.EmptyRequest;
import io.sermant.demo.tagtransmission.grpc.api.service.GrpcTagTransmissionServiceProto;
import io.sermant.demo.tagtransmission.grpc.api.service.TagTransmissionServiceGrpc;
import io.sermant.demo.tagtransmission.grpc.api.service.TrafficTag;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;

import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * grpc client端
 *
 * @author daizhenyu
 * @since 2023-10-13
 **/
@RestController
@RequestMapping(value = "grpc")
public class GrpcClientController {
    @Value("${grpc.server.port}")
    private int grpcServerPort;

    /**
     * 验证grpc透传流量标签，使用stub方式调用服务端
     *
     * @return 流量标签值
     */
    @RequestMapping(value = "testGrpcByStub", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String testGrpcByStub() {
        ManagedChannel originChannel = ManagedChannelBuilder.forAddress("localhost", grpcServerPort)
                .usePlaintext()
                .build();
        TagTransmissionServiceGrpc.TagTransmissionServiceBlockingStub stub = TagTransmissionServiceGrpc
                .newBlockingStub(originChannel);
        TrafficTag trafficTag = stub.transmitTag(EmptyRequest.newBuilder().build());
        originChannel.shutdown();
        return trafficTag.getTag();
    }

    /**
     * 验证grpc透传流量标签，使用dynamic message方式调用服务端
     *
     * @return 流量标签值
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @RequestMapping(value = "testGrpcByDynamicMessage", method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String testGrpcByDynamicMessage() throws ExecutionException, InterruptedException {
        ManagedChannel channel =
                ManagedChannelBuilder.forAddress("localhost", grpcServerPort).usePlaintext().build();

        Descriptors.MethodDescriptor originMethodDescriptor = generateProtobufMethodDescriptor();
        MethodDescriptor<DynamicMessage, DynamicMessage> methodDescriptor = generateGrpcMethodDescriptor(
                originMethodDescriptor);

        // 创建动态消息
        DynamicMessage request = DynamicMessage.newBuilder(originMethodDescriptor.getInputType()).build();

        // 使用 CompletableFuture 处理异步响应
        CallOptions callOptions = CallOptions.DEFAULT;
        CompletableFuture<DynamicMessage> responseFuture = new CompletableFuture<>();
        ClientCalls.asyncUnaryCall(channel.newCall(methodDescriptor, callOptions), request,
                new StreamObserver<DynamicMessage>() {
                    @Override
                    public void onNext(DynamicMessage value) {
                        responseFuture.complete(value);
                    }

                    @Override
                    public void onError(Throwable t) {
                        responseFuture.completeExceptionally(t);
                    }

                    @Override
                    public void onCompleted() {
                    }
                });

        // 等待异步响应完成
        DynamicMessage response = responseFuture.get();
        channel.shutdown();
        return (String) response.getField(originMethodDescriptor.getOutputType().findFieldByName("tag"));
    }

    private MethodDescriptor<DynamicMessage, DynamicMessage>
            generateGrpcMethodDescriptor(Descriptors.MethodDescriptor originMethodDescriptor) {
        // 生成方法全名
        String fullMethodName = MethodDescriptor
                .generateFullMethodName(originMethodDescriptor.getService().getFullName(),
                        originMethodDescriptor.getName());

        // 请求和响应类型
        MethodDescriptor.Marshaller<DynamicMessage> inputTypeMarshaller = ProtoUtils
                .marshaller(DynamicMessage.newBuilder(originMethodDescriptor.getInputType())
                        .buildPartial());
        MethodDescriptor.Marshaller<DynamicMessage> outputTypeMarshaller = ProtoUtils
                .marshaller(DynamicMessage.newBuilder(originMethodDescriptor.getOutputType())
                        .buildPartial());

        // 生成方法描述
        return MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
                .setFullMethodName(fullMethodName)
                .setRequestMarshaller(inputTypeMarshaller)
                .setResponseMarshaller(outputTypeMarshaller)
                // 使用 UNKNOWN，自动修改
                .setType(MethodDescriptor.MethodType.UNKNOWN)
                .build();
    }

    private Descriptors.MethodDescriptor generateProtobufMethodDescriptor() {
        // 构建服务存根
        Descriptors.FileDescriptor serviceFileDescriptor = GrpcTagTransmissionServiceProto.getDescriptor().getFile();
        Descriptors.ServiceDescriptor serviceDescriptor = serviceFileDescriptor
                .findServiceByName("TagTransmissionService");
        return serviceDescriptor.getMethods().get(0);
    }
}

package com.guneriu.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.IntStream;

@Slf4j
public class GrpcClient {

    @SneakyThrows
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090).usePlaintext(true).build();

        simpleRequestResponse(channel);

        serverSideStreaming(channel);

        serverSideStreamingAsync(channel);

        clientSideStreamingAsync(channel);

        Thread.sleep(1000 * 10);

        channel.shutdown();
    }

    private static void simpleRequestResponse(ManagedChannel channel) {
        HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);

        HelloRequest helloRequest = HelloRequest.newBuilder().setFirstName("John").setLastName("Wayne").build();
        HelloResponse helloResponse = stub.hello(helloRequest);
        log.info("\n\n got message {}", helloResponse);
    }

    private static void serverSideStreaming(ManagedChannel channel) {
        HelloServiceGrpc.HelloServiceBlockingStub stub = HelloServiceGrpc.newBlockingStub(channel);

        HelloRequest helloRequest = HelloRequest.newBuilder().setFirstName("John").setLastName("Wayne").build();
        stub.helloServerStream(helloRequest).forEachRemaining(response -> log.info("Got response {}", response));
    }

    private static void serverSideStreamingAsync(ManagedChannel channel) {
        HelloServiceGrpc.HelloServiceStub stub = HelloServiceGrpc.newStub(channel);

        HelloRequest helloRequest = HelloRequest.newBuilder().setFirstName("John").setLastName("Wayne").build();

        stub.helloServerStream(helloRequest, new StreamObserver<HelloResponse>() {
            @Override
            public void onNext(HelloResponse helloResponse) {
                log.info("got new message {}", helloResponse);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("got error: ", throwable);
            }

            @Override
            public void onCompleted() {
                log.info("Data transmission is completed.");
            }
        });
    }

    private static void clientSideStreamingAsync(ManagedChannel channel) {
        HelloServiceGrpc.HelloServiceStub stub = HelloServiceGrpc.newStub(channel);

        StreamObserver<HelloRequest> responseObserver =
                stub.helloClientStream(new StreamObserver<HelloResponse>() {
            @Override
            public void onNext(HelloResponse helloResponse) {
                log.info("got new message {}", helloResponse);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("got error: ", throwable);
            }

            @Override
            public void onCompleted() {
                log.info("Data transmission is completed.");
            }
        });

        IntStream.range(0, 10)
                .forEachOrdered(num -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log.error("got InterruptedException", e);
                    }
                    HelloRequest request = HelloRequest.newBuilder()
                            .setFirstName(String.format("firstName: %d", num))
                            .setLastName(String.format("lastName: %d", num))
                            .build();
                    log.info("Sending message to Server {}", request);
                    responseObserver.onNext(request);
                });

        log.info("Completed data transmission");

        responseObserver.onCompleted();
    }

}


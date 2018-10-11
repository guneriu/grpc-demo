package com.guneriu.grpc;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
public class HelloServiceImpl extends HelloServiceGrpc.HelloServiceImplBase {

    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {

        String greeting = new StringBuilder().append("Hello, ")
                .append(request.getFirstName())
                .append(" ")
                .append(request.getLastName())
                .toString();

        HelloResponse response = HelloResponse.newBuilder().setGreeting(greeting).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void helloServerStream(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        IntStream.range(0, 10)
                .forEachOrdered(num -> {
                    HelloResponse response = HelloResponse.newBuilder()
                            .setGreeting(String.format("Message %d", num))
                            .build();
                    log.info("Sending message to client {}", response);
                    responseObserver.onNext(response);
                });

        log.info("Completed data transmission");

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<HelloRequest> helloClientStream(StreamObserver<HelloResponse> responseObserver) {
        return new StreamObserver<HelloRequest>() {
            private AtomicInteger messageCounter = new AtomicInteger(0);
            @Override
            public void onNext(HelloRequest helloRequest) {
                log.info("Got request from client {}", helloRequest);
                HelloResponse response = HelloResponse.newBuilder()
                        .setGreeting(String.format("Message %d", messageCounter.incrementAndGet()))
                        .build();
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("Got error ", throwable);
            }

            @Override
            public void onCompleted() {
                log.info("Transmission from request completed, sending back response");
                responseObserver.onCompleted();
            }
        };
    }

}

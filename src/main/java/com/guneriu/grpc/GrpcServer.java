package com.guneriu.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcServer {

    @SneakyThrows
    public static void main(String[] args) {
        Server server = ServerBuilder.forPort(9090).addService(new HelloServiceImpl()).build();

        log.info("*** Starting the Server *****");
        server.start();
        log.info("*** Started the Server *****");

        server.awaitTermination();
    }

}

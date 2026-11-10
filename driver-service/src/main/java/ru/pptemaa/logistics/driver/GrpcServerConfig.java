package ru.pptemaa.logistics.driver;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class GrpcServerConfig {
    @Bean(initMethod = "start", destroyMethod = "shutdown")
    Server driverGrpcServer(DriverGrpcService service,
            @Value("${driver.grpc.port}") int port) {
        return ServerBuilder.forPort(port).addService(service).build();
    }
}

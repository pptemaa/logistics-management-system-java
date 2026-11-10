package ru.pptemaa.logistics.order;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.pptemaa.logistics.contracts.driver.DriverDirectoryGrpc;
import ru.pptemaa.logistics.contracts.driver.DriverInfo;
import ru.pptemaa.logistics.contracts.driver.GetDriverRequest;

@Component
public class DriverDirectoryClient {
    private final ManagedChannel channel;
    private final DriverDirectoryGrpc.DriverDirectoryBlockingStub stub;

    public DriverDirectoryClient(@Value("${driver.grpc.host}") String host,
            @Value("${driver.grpc.port}") int port) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        stub = DriverDirectoryGrpc.newBlockingStub(channel);
    }

    public DriverInfo getDriver(long driverId) {
        try {
            return stub.withDeadlineAfter(3, TimeUnit.SECONDS)
                    .getDriver(GetDriverRequest.newBuilder().setDriverId(driverId).build());
        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown driver");
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Driver service unavailable");
        }
    }

    @PreDestroy
    public void close() {
        channel.shutdown();
    }
}

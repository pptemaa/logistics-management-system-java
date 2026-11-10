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
import ru.pptemaa.logistics.contracts.warehouse.GetProductRequest;
import ru.pptemaa.logistics.contracts.warehouse.ProductInfo;
import ru.pptemaa.logistics.contracts.warehouse.WarehouseCatalogGrpc;

@Component
public class WarehouseCatalogClient {
    private final ManagedChannel channel;
    private final WarehouseCatalogGrpc.WarehouseCatalogBlockingStub stub;

    public WarehouseCatalogClient(@Value("${warehouse.grpc.host}") String host,
            @Value("${warehouse.grpc.port}") int port) {
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        stub = WarehouseCatalogGrpc.newBlockingStub(channel);
    }

    public ProductInfo getProduct(long productId) {
        try {
            return stub.withDeadlineAfter(3, TimeUnit.SECONDS)
                    .getProduct(GetProductRequest.newBuilder().setProductId(productId).build());
        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown product");
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Warehouse service unavailable");
        }
    }

    @PreDestroy
    public void close() {
        channel.shutdown();
    }
}

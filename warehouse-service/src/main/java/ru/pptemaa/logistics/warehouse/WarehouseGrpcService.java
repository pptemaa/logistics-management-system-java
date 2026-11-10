package ru.pptemaa.logistics.warehouse;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import ru.pptemaa.logistics.contracts.warehouse.GetProductRequest;
import ru.pptemaa.logistics.contracts.warehouse.ProductInfo;
import ru.pptemaa.logistics.contracts.warehouse.WarehouseCatalogGrpc;
import org.springframework.stereotype.Service;

@Service
public class WarehouseGrpcService extends WarehouseCatalogGrpc.WarehouseCatalogImplBase {
    private final StockRepository stock;

    public WarehouseGrpcService(StockRepository stock) {
        this.stock = stock;
    }

    @Override
    public void getProduct(GetProductRequest request, StreamObserver<ProductInfo> response) {
        StockItem item = stock.findById(request.getProductId()).orElse(null);
        if (item == null) {
            response.onError(Status.NOT_FOUND.withDescription("Product not found").asRuntimeException());
            return;
        }
        response.onNext(ProductInfo.newBuilder()
                .setProductId(item.productId())
                .setProductName(item.productName())
                .setAvailableQuantity(item.quantity())
                .setPriceCents(item.price().movePointRight(2).longValueExact())
                .build());
        response.onCompleted();
    }
}

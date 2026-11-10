package ru.pptemaa.logistics.driver;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;
import ru.pptemaa.logistics.contracts.driver.DriverDirectoryGrpc;
import ru.pptemaa.logistics.contracts.driver.DriverInfo;
import ru.pptemaa.logistics.contracts.driver.GetDriverRequest;

@Service
public class DriverGrpcService extends DriverDirectoryGrpc.DriverDirectoryImplBase {
    private final DriverRepository drivers;

    public DriverGrpcService(DriverRepository drivers) {
        this.drivers = drivers;
    }

    @Override
    public void getDriver(GetDriverRequest request, StreamObserver<DriverInfo> response) {
        Driver driver = drivers.findById(request.getDriverId()).orElse(null);
        if (driver == null) {
            response.onError(Status.NOT_FOUND.withDescription("Driver not found").asRuntimeException());
            return;
        }
        response.onNext(DriverInfo.newBuilder().setDriverId(driver.id())
                .setStatus(driver.status().value()).build());
        response.onCompleted();
    }
}

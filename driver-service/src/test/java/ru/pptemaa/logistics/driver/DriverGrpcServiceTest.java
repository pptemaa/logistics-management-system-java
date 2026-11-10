package ru.pptemaa.logistics.driver;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.pptemaa.logistics.contracts.driver.DriverInfo;
import ru.pptemaa.logistics.contracts.driver.GetDriverRequest;

class DriverGrpcServiceTest {
    @Test
    void returnsCurrentDriverStatus() {
        DriverRepository repository = mock(DriverRepository.class);
        when(repository.findById(42)).thenReturn(Optional.of(new Driver(
                42, "Ivan Petrov", "+79990000000", "TEST-123", "Lada", DriverStatus.AVAILABLE)));
        @SuppressWarnings("unchecked")
        StreamObserver<DriverInfo> response = mock(StreamObserver.class);

        new DriverGrpcService(repository).getDriver(
                GetDriverRequest.newBuilder().setDriverId(42).build(), response);

        ArgumentCaptor<DriverInfo> result = ArgumentCaptor.forClass(DriverInfo.class);
        verify(response).onNext(result.capture());
        verify(response).onCompleted();
        org.junit.jupiter.api.Assertions.assertEquals("available", result.getValue().getStatus());
    }

    @Test
    void reportsMissingDriver() {
        DriverRepository repository = mock(DriverRepository.class);
        when(repository.findById(999)).thenReturn(Optional.empty());
        @SuppressWarnings("unchecked")
        StreamObserver<DriverInfo> response = mock(StreamObserver.class);

        new DriverGrpcService(repository).getDriver(
                GetDriverRequest.newBuilder().setDriverId(999).build(), response);

        ArgumentCaptor<Throwable> error = ArgumentCaptor.forClass(Throwable.class);
        verify(response).onError(error.capture());
        org.junit.jupiter.api.Assertions.assertEquals(Status.Code.NOT_FOUND,
                ((StatusRuntimeException) error.getValue()).getStatus().getCode());
    }
}

package ru.pptemaa.logistics.gateway;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayController {
    private final HttpClient client;
    private final String authUrl;
    private final String orderUrl;
    private final String driverUrl;
    private final String warehouseUrl;

    public GatewayController(@Value("${gateway.auth.url}") String authUrl,
            @Value("${gateway.order.url}") String orderUrl,
            @Value("${gateway.driver.url}") String driverUrl,
            @Value("${gateway.warehouse.url}") String warehouseUrl) {
        this(HttpClient.newHttpClient(), authUrl, orderUrl, driverUrl, warehouseUrl);
    }

    GatewayController(HttpClient client, String authUrl, String orderUrl,
            String driverUrl, String warehouseUrl) {
        this.client = client;
        this.authUrl = authUrl;
        this.orderUrl = orderUrl;
        this.driverUrl = driverUrl;
        this.warehouseUrl = warehouseUrl;
    }

    @RequestMapping({"/api/auth/**", "/api/orders/**", "/api/drivers/**", "/api/stock/**"})
    public ResponseEntity<byte[]> route(HttpServletRequest request) throws IOException {
        String path = request.getRequestURI();
        String base = path.startsWith("/api/auth/") ? authUrl
                : path.startsWith("/api/orders/") || path.equals("/api/orders") ? orderUrl
                : path.startsWith("/api/drivers/") || path.equals("/api/drivers") ? driverUrl
                : warehouseUrl;
        String target = base + path + (request.getQueryString() == null
                ? "" : "?" + request.getQueryString());
        HttpRequest.Builder upstream = HttpRequest.newBuilder(URI.create(target))
                .method(request.getMethod(), HttpRequest.BodyPublishers.ofByteArray(
                        request.getInputStream().readAllBytes()));
        if (request.getContentType() != null) {
            upstream.header(HttpHeaders.CONTENT_TYPE, request.getContentType());
        }
        if (request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
            upstream.header(HttpHeaders.AUTHORIZATION,
                    request.getHeader(HttpHeaders.AUTHORIZATION));
        }
        try {
            HttpResponse<byte[]> response = client.send(upstream.build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            ResponseEntity.BodyBuilder result = ResponseEntity.status(response.statusCode());
            response.headers().firstValue(HttpHeaders.CONTENT_TYPE)
                    .ifPresent(value -> result.header(HttpHeaders.CONTENT_TYPE, value));
            response.headers().firstValue(HttpHeaders.LOCATION)
                    .ifPresent(value -> result.header(HttpHeaders.LOCATION, value));
            return result.body(response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (IOException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}

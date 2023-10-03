package no.ks.fiks.io.klient;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.util.TimeValue;

import java.time.Duration;
import java.util.function.Function;

/**
 * Builder that must be used to create
 */
@Slf4j
public class FiksIOUtsendingKlientBuilder {

    private String scheme = "https";

    private String hostName;

    private Integer portNumber;

    private AuthenticationStrategy authenticationStrategy;

    private Function<ClassicHttpRequest, ClassicHttpRequest> requestInterceptor;

    private ObjectMapper objectMapper;

    private CloseableHttpClient httpClient = HttpClients.custom()
            .disableAutomaticRetries()
            .useSystemProperties()
            .evictIdleConnections(TimeValue.of(Duration.ofMinutes(1L)))
            .build();

    public FiksIOUtsendingKlientBuilder withHttpClient(@NonNull final CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public FiksIOUtsendingKlientBuilder withScheme(@NonNull final String scheme) {
        this.scheme = scheme;
        return this;
    }

    public FiksIOUtsendingKlientBuilder withHostName(@NonNull final String hostName) {
        this.hostName = hostName;
        return this;
    }

    public FiksIOUtsendingKlientBuilder withPortNumber(@NonNull final Integer portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    public FiksIOUtsendingKlientBuilder withAuthenticationStrategy(@NonNull final AuthenticationStrategy authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
        return this;
    }

    public FiksIOUtsendingKlientBuilder withRequestInterceptor(Function<ClassicHttpRequest, ClassicHttpRequest> requestInterceptor) {
        this.requestInterceptor = requestInterceptor;
        return this;
    }

    public FiksIOUtsendingKlientBuilder withObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    public FiksIOUtsendingKlient build() {

        return new FiksIOUtsendingKlient(
                createRequestFactory(),
                authenticationStrategy,
                getOrCreateRequestInterceptor(),
                getOrCreateObjectMapper(),
                httpClient
        );
    }

    private RequestFactory createRequestFactory() {
        return RequestFactoryImpl.builder()
                                 .scheme(scheme)
                                 .hostName(hostName)
                                 .portNumber(portNumber)
                                 .build();
    }

    private Function<ClassicHttpRequest, ClassicHttpRequest> getOrCreateRequestInterceptor() {
        return requestInterceptor == null ? request -> request : requestInterceptor;
    }

    private ObjectMapper getOrCreateObjectMapper() {
        return objectMapper == null ? new ObjectMapper().findAndRegisterModules() : objectMapper;
    }
}

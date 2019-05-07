package no.ks.fiks.io.klient;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;

import java.util.function.Function;

/**
 * Builder that must be used to create
 */
@Slf4j
public class FiksIOUtsendingKlientBuilder {

    private HttpClient httpClient;

    private String scheme = "https";

    private String hostName;

    private Integer portNumber;

    private AuthenticationStrategy authenticationStrategy;

    private Function<Request, Request> requestInterceptor;

    private ObjectMapper objectMapper;

    public FiksIOUtsendingKlientBuilder withHttpClient(@NonNull final HttpClient httpClient) {
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

    public FiksIOUtsendingKlientBuilder withRequestInterceptor(Function<Request, Request> requestInterceptor) {
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
                getOrCreateObjectMapper()
        );
    }

    private RequestFactory createRequestFactory() {
        return RequestFactoryImpl.builder()
                                 .client(getOrCreateHttpClient())
                                 .scheme(scheme)
                                 .hostName(hostName)
                                 .portNumber(portNumber)
                                 .build();
    }

    private Function<Request, Request> getOrCreateRequestInterceptor() {
        return requestInterceptor == null ? request -> request : requestInterceptor;
    }

    private ObjectMapper getOrCreateObjectMapper() {
        return objectMapper == null ? new ObjectMapper().findAndRegisterModules() : objectMapper;
    }

    private HttpClient getOrCreateHttpClient() {
        final HttpClient internalClient = httpClient == null ? new HttpClient() : httpClient;
        if(! internalClient.isStarted()) {
            log.debug("Starting http client");
            try {
                internalClient.start();
            } catch (Exception e) {
                log.warn("Feil under oppstart av Jetty HttpClient", e);
                throw new IllegalStateException("Kunne ikke starte http client", e);
            }
        }
        return internalClient;
    }
}

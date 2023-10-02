package no.ks.fiks.io.klient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.entity.mime.InputStreamBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.util.TimeValue;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;


@Slf4j
public class FiksIOUtsendingKlient implements Closeable {

    private final RequestFactory requestFactory;
    private final AuthenticationStrategy authenticationStrategy;
    private final Function<ClassicHttpRequest, ClassicHttpRequest> requestInterceptor;
    private final CloseableHttpClient client;
    private final ObjectMapper objectMapper;

    FiksIOUtsendingKlient(@NonNull final RequestFactory requestFactory,
                          @NonNull AuthenticationStrategy authenticationStrategy,
                          @NonNull Function<ClassicHttpRequest, ClassicHttpRequest> requestInterceptor,
                          @NonNull final ObjectMapper objectMapper) {
        this.requestFactory = requestFactory;
        this.authenticationStrategy = authenticationStrategy;
        this.requestInterceptor = requestInterceptor;
        this.objectMapper = objectMapper;
        this.client = HttpClients.custom()
                .disableAutomaticRetries()
                .useSystemProperties()
                .evictIdleConnections(TimeValue.of(Duration.ofMinutes(1L)))
                .build();
    }

    public static FiksIOUtsendingKlientBuilder builder() {
        return new FiksIOUtsendingKlientBuilder();
    }

    public SendtMeldingApiModel send(@NonNull MeldingSpesifikasjonApiModel metadata, @NonNull Optional<InputStream> data) {
        final ClassicHttpRequest request = requestFactory.createSendToFiksIORequest(createMultiPartContent(metadata, data));
        authenticationStrategy.setAuthenticationHeaders(request);
        requestInterceptor.apply(request);

        try {
            return client.execute(request, response -> {
                final int responseCode = response.getCode();
                log.debug("Response status: {}", responseCode);
                if(responseCode >= HttpStatus.SC_BAD_REQUEST) {
                    final var contentAsString = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    throw new FiksIOHttpException(String.format("HTTP-feil under sending av melding (%d): %s", responseCode, contentAsString), responseCode, contentAsString);
                }
                return objectMapper.readValue(response.getEntity().getContent(), SendtMeldingApiModel.class);
            });
        } catch (IOException e) {
            throw new RuntimeException("Feil under invokering av FIKS IO api", e);
        }

    }

    private HttpEntity createMultiPartContent(@NonNull MeldingSpesifikasjonApiModel metadata, @NonNull Optional<InputStream> data) {
        var multipartRequestContentBuilder = MultipartEntityBuilder.create()
                .addBinaryBody("metadata", serialiser(metadata).getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_JSON, null);
        data.ifPresent(inputStream ->
                multipartRequestContentBuilder.addPart("data", new InputStreamBody(inputStream, (String) null)));
        return multipartRequestContentBuilder.build();
    }

    private String serialiser(@NonNull Object metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Feil under serialisering av metadata", e);
        }
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}

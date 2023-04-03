package no.ks.fiks.io.klient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static org.eclipse.jetty.http.HttpStatus.isClientError;
import static org.eclipse.jetty.http.HttpStatus.isServerError;

@Slf4j
public class FiksIOUtsendingKlient implements Closeable {

    private final RequestFactory requestFactory;
    private final AuthenticationStrategy authenticationStrategy;
    private final Function<Request, Request> requestInterceptor;

    private final ObjectMapper objectMapper;

    FiksIOUtsendingKlient(@NonNull final RequestFactory requestFactory,
                          @NonNull AuthenticationStrategy authenticationStrategy,
                          @NonNull Function<Request, Request> requestInterceptor,
                          @NonNull final ObjectMapper objectMapper) {
        this.requestFactory = requestFactory;
        this.authenticationStrategy = authenticationStrategy;
        this.requestInterceptor = requestInterceptor;
        this.objectMapper = objectMapper;
    }

    public static FiksIOUtsendingKlientBuilder builder() {
        return new FiksIOUtsendingKlientBuilder();
    }

    public SendtMeldingApiModel send(@NonNull MeldingSpesifikasjonApiModel metadata, @NonNull Optional<InputStream> data) {
        try (MultiPartRequestContent contentProvider = new MultiPartRequestContent()) {
            contentProvider.addFieldPart("metadata", new StringRequestContent("application/json", serialiser(metadata), StandardCharsets.UTF_8), null);
            data.ifPresent(inputStream ->
                    contentProvider.addFilePart("data", UUID.randomUUID().toString(), new InputStreamRequestContent(inputStream), null));

            InputStreamResponseListener listener = new InputStreamResponseListener();
            final Request request = requestFactory.createSendToFiksIORequest(contentProvider);

            authenticationStrategy.setAuthenticationHeaders(request);

            requestInterceptor.apply(request).send(listener);

            try (InputStream listenerInputStream = listener.getInputStream()) {
                Response response = listener.get(1, TimeUnit.HOURS);
                if (isClientError(response.getStatus()) || isServerError(response.getStatus())) {
                    int status = response.getStatus();
                    String content = IOUtils.toString(listenerInputStream, StandardCharsets.UTF_8);
                    throw new FiksIOHttpException(String.format("HTTP-feil under sending av melding (%d): %s", status, content), status, content);
                }
                return objectMapper.readValue(listenerInputStream, SendtMeldingApiModel.class);
            } catch (InterruptedException | TimeoutException | ExecutionException | IOException e) {
                throw new RuntimeException("Feil under invokering av FIKS IO api", e);
            }
        }

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
        requestFactory.close();
    }
}

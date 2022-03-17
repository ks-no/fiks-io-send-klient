package no.ks.fiks.io.klient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Option;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
    private Function<Request, Request> requestInterceptor;

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

    public SendtMeldingApiModel send(@NonNull MeldingSpesifikasjonApiModel metadata, @NonNull Option<InputStream> data) {
            MultiPartRequestContent contentProvider = new MultiPartRequestContent();
            contentProvider.addFieldPart("metadata", new StringRequestContent("application/json", serialiser(metadata), Charset.forName("UTF-8")), null);
            if (data.isDefined())
                contentProvider.addFilePart("data", UUID.randomUUID().toString(), new InputStreamRequestContent(data.get()), null);


            InputStreamResponseListener listener = new InputStreamResponseListener();
            final Request request = requestFactory.createSendToFiksIORequest(contentProvider);

            authenticationStrategy.setAuthenticationHeaders(request);

            contentProvider.close();
            requestInterceptor.apply(request).send(listener);

            try {
                Response response = listener.get(1, TimeUnit.HOURS);
                if (isClientError(response.getStatus()) || isServerError(response.getStatus())) {
                    int status = response.getStatus();
                    String content = IOUtils.toString(listener.getInputStream(), StandardCharsets.UTF_8);
                    throw new FiksIOHttpException(String.format("HTTP-feil under sending av melding (%d): %s", status, content), status, content);
                }
                return objectMapper.readValue(listener.getInputStream(), SendtMeldingApiModel.class);
            } catch (InterruptedException | TimeoutException | ExecutionException | IOException e) {
                throw new RuntimeException("Feil under invokering av FIKS IO api", e);
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

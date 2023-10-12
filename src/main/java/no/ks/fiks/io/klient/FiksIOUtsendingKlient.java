package no.ks.fiks.io.klient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
public class FiksIOUtsendingKlient implements Closeable {

    private final RequestFactory requestFactory;
    private final AuthenticationStrategy authenticationStrategy;
    private final Function<ClassicHttpRequest, ClassicHttpRequest> requestInterceptor;
    private final CloseableHttpClient client;
    private final ObjectMapper objectMapper;

    private final static int END_OF_STREAM = -1;
    public static final String MULTIPART_METADATA = "metadata";
    public static final String MULTIPART_DATA = "data";

    FiksIOUtsendingKlient(@NonNull final RequestFactory requestFactory,
                          @NonNull AuthenticationStrategy authenticationStrategy,
                          @NonNull Function<ClassicHttpRequest, ClassicHttpRequest> requestInterceptor,
                          @NonNull final ObjectMapper objectMapper,
                          @NonNull final CloseableHttpClient client) {
        this.requestFactory = requestFactory;
        this.authenticationStrategy = authenticationStrategy;
        this.requestInterceptor = requestInterceptor;
        this.objectMapper = objectMapper;
        this.client = client;
    }

    public static FiksIOUtsendingKlientBuilder builder() {
        return new FiksIOUtsendingKlientBuilder();
    }

    public SendtMeldingApiModel send(@NonNull MeldingSpesifikasjonApiModel metadata, @NonNull Optional<InputStream> data) {
        if (data.isPresent()) {
            return send(metadata, data.get());
        } else {
            return send(metadata);
        }
    }
    private SendtMeldingApiModel send(@NonNull MeldingSpesifikasjonApiModel metadata) {
        return sendMelding(getMultipartEntityBuilder(metadata).build());
    }

    private SendtMeldingApiModel send(@NonNull MeldingSpesifikasjonApiModel metadata, @NonNull InputStream data) {
        try (PushbackInputStream pis = new PushbackInputStream(data)) {
            int read = pis.read();
            if (read == END_OF_STREAM) {
                throw new IllegalArgumentException("Klarte ikke å lese innhold i fil");
            }
            pis.unread(read);

            var multipartRequestContentBuilder = getMultipartEntityBuilder(metadata);
            multipartRequestContentBuilder.addBinaryBody(MULTIPART_DATA, pis, ContentType.APPLICATION_OCTET_STREAM, UUID.randomUUID().toString());
            HttpEntity httpEntity =  multipartRequestContentBuilder.build();

            return sendMelding(httpEntity);
        } catch (IOException e) {
            throw new RuntimeException("Feil under lesing av data, som skal sendes med Fiks-IO melding", e);
        }
    }

    private MultipartEntityBuilder getMultipartEntityBuilder(MeldingSpesifikasjonApiModel metadata) {
        return MultipartEntityBuilder.create()
                .addBinaryBody(MULTIPART_METADATA, serialiser(metadata).getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_JSON, null);
    }

    private SendtMeldingApiModel sendMelding(HttpEntity httpEntity) {
        try {
            final ClassicHttpRequest request = requestFactory.createSendToFiksIORequest(httpEntity);
            authenticationStrategy.setAuthenticationHeaders(request);
            requestInterceptor.apply(request);
            return client.execute(request, response -> {
                final int responseCode = response.getCode();
                log.debug("Response status: {}", responseCode);
                try (final HttpEntity entity = response.getEntity();
                     final InputStream content = entity.getContent()) {
                    if (responseCode >= HttpStatus.SC_BAD_REQUEST) {
                        final var contentAsString = IOUtils.toString(content, StandardCharsets.UTF_8);
                        throw new FiksIOHttpException(String.format("HTTP-feil under sending av melding (%d): %s", responseCode, contentAsString), responseCode, contentAsString);
                    }
                    return objectMapper.readValue(content, SendtMeldingApiModel.class);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Feil under sending av Fiks-IO melding", e);
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
        client.close();
    }
}

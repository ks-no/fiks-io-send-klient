package no.ks.fiks.io.klient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.model.JsonBody;
import org.mockserver.verify.VerificationTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith({MockServerExtension.class, MockitoExtension.class})
class FiksIOUtsendingKlientTest {

    private static final Logger LOG = LoggerFactory.getLogger(FiksIOUtsendingKlientTest.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules()
            .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private static final String SEND_PATH = RequestFactoryImpl.BASE_PATH + "send";

    @DisplayName("Sender en pakke")
    @Test
    void send(ClientAndServer clientAndServer) throws IOException {
        final MeldingSpesifikasjonApiModel meldingSpesifikasjonApiModel = MeldingSpesifikasjonApiModel.builder()
                .meldingType("type")
                .avsenderKontoId(UUID.randomUUID())
                .mottakerKontoId(UUID.randomUUID())
                .build();
        clientAndServer.when(request().withMethod("POST").withPath(SEND_PATH))
                .respond(request -> response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withDelay(TimeUnit.MILLISECONDS, 500L)
                        .withBody(new JsonBody(MAPPER.writeValueAsString(
                                SendtMeldingApiModel.builder()
                                        .avsenderKontoId(meldingSpesifikasjonApiModel.getAvsenderKontoId())
                                        .mottakerKontoId(meldingSpesifikasjonApiModel.getMottakerKontoId())
                                        .meldingType(meldingSpesifikasjonApiModel.getMeldingType())
                                        .meldingId(UUID.randomUUID())
                                        .build()))));
        final FiksIOUtsendingKlient fiksIOUtsendingKlient = FiksIOUtsendingKlient.builder()
                .withHostName("localhost")
                .withPortNumber(clientAndServer.getPort())
                .withScheme("http")
                .withAuthenticationStrategy(new IntegrasjonAuthenticationStrategy(() -> "token", UUID.randomUUID(), "passord"))
                .build();
        try (InputStream payload = FiksIOUtsendingKlientTest.class.getResourceAsStream("/small.pdf")) {
            assertThat(fiksIOUtsendingKlient.send(meldingSpesifikasjonApiModel, Optional.ofNullable(payload))).isInstanceOfAny(SendtMeldingApiModel.class);
            clientAndServer.verify(request(SEND_PATH).withMethod("POST"));
        }
    }

    @DisplayName("Sender flere pakker samtidig")
    @Test
    void sendMange(ClientAndServer clientAndServer) throws IOException {
        final MeldingSpesifikasjonApiModel meldingSpesifikasjonApiModel = MeldingSpesifikasjonApiModel.builder()
                .meldingType("type")
                .avsenderKontoId(UUID.randomUUID())
                .mottakerKontoId(UUID.randomUUID())
                .build();
        clientAndServer.when(request().withMethod("POST").withPath(SEND_PATH))
                .respond(request -> response()
                        .withStatusCode(HttpStatusCode.ACCEPTED_202.code())
                        .withDelay(TimeUnit.MILLISECONDS, 500L)
                        .withBody(new JsonBody(MAPPER.writeValueAsString(
                                SendtMeldingApiModel.builder()
                                        .avsenderKontoId(meldingSpesifikasjonApiModel.getAvsenderKontoId())
                                        .mottakerKontoId(meldingSpesifikasjonApiModel.getMottakerKontoId())
                                        .meldingType(meldingSpesifikasjonApiModel.getMeldingType())
                                        .meldingId(UUID.randomUUID())
                                        .build()))));

        try (final var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .build();
             CloseableHttpClient client = HttpClients.custom()
                     .disableAutomaticRetries()
                     .useSystemProperties()
                     .setConnectionManager(connectionManager)
                     .evictIdleConnections(TimeValue.of(Duration.ofMinutes(1L)))
                     .build()) {

            final FiksIOUtsendingKlient fiksIOUtsendingKlient = FiksIOUtsendingKlient.builder()
                    .withHostName("localhost")
                    .withPortNumber(clientAndServer.getPort())
                    .withScheme("http")
                    .withAuthenticationStrategy(new IntegrasjonAuthenticationStrategy(() -> "token", UUID.randomUUID(), "passord"))
                    .withHttpClient(client)
                    .build();
            final var payload = IOUtils.toByteArray(Objects.requireNonNull(FiksIOUtsendingKlientTest.class.getResourceAsStream("/small.pdf")));
            final int antallTreads = 30;
            final ExecutorService executor = Executors.newFixedThreadPool(antallTreads);
            try {

                var futures = new LinkedList<CompletableFuture<SendtMeldingApiModel>>();
                for (int i = 0; i < antallTreads; i++) {
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        try (var meldingStream = new ByteArrayInputStream(payload)) {
                            return fiksIOUtsendingKlient.send(meldingSpesifikasjonApiModel, Optional.of(meldingStream));
                        } catch (IOException e) {
                            throw new RuntimeException("Feil under sending av melding", e);
                        }
                    }, executor));
                }
                futures.stream().map(CompletableFuture::join).forEach((s) ->
                        assertThat(s).isInstanceOf(SendtMeldingApiModel.class));
                clientAndServer.verify(request(SEND_PATH).withMethod("POST"), VerificationTimes.atLeast(antallTreads));
                assertThat(connectionManager.getTotalStats().getLeased()).isEqualTo(0);
                assertThat(connectionManager.getTotalStats().getPending()).isEqualTo(0);
            } finally {
                executor.shutdown();
            }
        }
    }
}
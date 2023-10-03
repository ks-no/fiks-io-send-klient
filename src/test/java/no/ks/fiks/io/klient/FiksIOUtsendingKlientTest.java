package no.ks.fiks.io.klient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.model.JsonBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith({MockServerExtension.class, MockitoExtension.class})
class FiksIOUtsendingKlientTest {

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
        try(InputStream payload = FiksIOUtsendingKlientTest.class.getResourceAsStream("/small.pdf")) {
            assertThat(fiksIOUtsendingKlient.send(meldingSpesifikasjonApiModel, Optional.ofNullable(payload))).isInstanceOfAny(SendtMeldingApiModel.class);
            clientAndServer.verify(request(SEND_PATH).withMethod("POST"));
        }
    }
}
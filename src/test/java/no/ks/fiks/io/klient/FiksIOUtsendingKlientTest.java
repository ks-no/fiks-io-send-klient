package no.ks.fiks.io.klient;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Option;
import no.ks.fiks.maskinporten.AccessTokenRequest;
import no.ks.fiks.maskinporten.MaskinportenklientOperations;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.model.JsonBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyMap;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockServerExtension.class)
class FiksIOUtsendingKlientTest {

    @DisplayName("Send Fiks IO message the Fiks IO endpoint")
    @Test
    void send(MockServerClient mockServerClient) throws IOException {

        UUID avsenderKontoId = UUID.randomUUID();
        String meldingType = "meldingType";
        UUID mottakerKontoId = UUID.randomUUID();
        long ttl = TimeUnit.HOURS.toMillis(1L);
        mockServerClient
                .when(request()
                        .withPath("/fiks-io/api/v1/send"))
                .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody(JsonBody.json(SendtMeldingApiModel.builder()
                                        .avsenderKontoId(avsenderKontoId)
                                        .mottakerKontoId(mottakerKontoId)
                                        .meldingId(UUID.randomUUID())
                                        .meldingType(meldingType)
                                        .headere(emptyMap())
                                        .ttl(ttl)
                                .build())));
        MaskinportenklientOperations maskinportenklient = Mockito.mock(MaskinportenklientOperations.class);
        when(maskinportenklient.getAccessToken(isA(AccessTokenRequest.class)))
                .thenReturn(RandomStringUtils.randomAlphanumeric(512));
        try(FiksIOUtsendingKlient fiksIOUtsendingKlient = FiksIOUtsendingKlient.builder()
                .withHostName("localhost")
                .withPortNumber(mockServerClient.getPort())
                .withScheme("http")
                .withObjectMapper(new ObjectMapper().findAndRegisterModules())
                .withAuthenticationStrategy(new IntegrasjonAuthenticationStrategy(maskinportenklient, UUID.randomUUID(), RandomStringUtils.randomAlphanumeric(128)))
                .build()) {

            MeldingSpesifikasjonApiModel meldingSpesifikasjonApiModel = MeldingSpesifikasjonApiModel.builder()
                    .avsenderKontoId(avsenderKontoId)
                    .meldingType(meldingType)
                    .mottakerKontoId(mottakerKontoId)
                    .ttl(ttl).build();
            String content = "{}";
            try(ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
                fiksIOUtsendingKlient.send(meldingSpesifikasjonApiModel, Option.of(inputStream));
            }
        }
    }
}
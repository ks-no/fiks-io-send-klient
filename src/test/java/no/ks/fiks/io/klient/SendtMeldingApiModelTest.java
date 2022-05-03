package no.ks.fiks.io.klient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SendtMeldingApiModelTest {

    @DisplayName("Serialize to JSON and back")
    @Test
    void serializeToJSONAndBack(TestInfo testInfo) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        final SendtMeldingApiModel meldingApiModel = SendtMeldingApiModel.builder()
                .meldingId(UUID.randomUUID())
                .avsenderKontoId(UUID.randomUUID())
                .mottakerKontoId(UUID.randomUUID())
                .headere(Collections.emptyMap())
                .ttl(Long.MAX_VALUE)
                .meldingType(testInfo.getDisplayName())
                .build();
        final String jsonString = mapper.writeValueAsString(meldingApiModel);
        assertNotNull(jsonString);
        SendtMeldingApiModel deserializedMeldingModel = mapper.readValue(jsonString, SendtMeldingApiModel.class);
        assertEquals(meldingApiModel, deserializedMeldingModel);
    }

}

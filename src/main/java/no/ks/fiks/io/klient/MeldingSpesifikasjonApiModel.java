package no.ks.fiks.io.klient;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.UUID;

@Value
@Builder
public class MeldingSpesifikasjonApiModel {
    @NotNull
    private UUID avsenderKontoId;
    @NotNull private UUID mottakerKontoId;
    @NotNull private String meldingType;
    private UUID svarPaMelding;
    private Long ttl;
    private Map<String, String> headere;
}

package no.ks.fiks.io.klient;

import no.ks.fiks.maskinporten.AccessTokenRequest;
import no.ks.fiks.maskinporten.MaskinportenklientOperations;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static no.ks.fiks.io.klient.IntegrasjonAuthenticationStrategy.INTEGRASJON_ID;
import static no.ks.fiks.io.klient.IntegrasjonAuthenticationStrategy.INTEGRASJON_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrasjonAuthenticationStrategyTest {

    @Captor
    private ArgumentCaptor<BasicHeader> headerCaptor;

    @DisplayName("Setter headers for integrasjonspålogging")
    @Test
    void setAuthenticationHeaders(@Mock MaskinportenklientOperations maskinportenklient, @Mock ClassicHttpRequest request) {
        final UUID integrasjonId = UUID.randomUUID();
        final String integrasjonPassord = "passord";

        when(maskinportenklient.getAccessToken(isA(AccessTokenRequest.class))).thenReturn("token");

        new IntegrasjonAuthenticationStrategy(maskinportenklient, integrasjonId, integrasjonPassord)
                .setAuthenticationHeaders(request);

        verify(request, times(3)).addHeader(headerCaptor.capture());
        final var headers = headerCaptor.getAllValues();
        assertThat(headers).hasSize(3);
        assertThat(headers.stream().map(BasicHeader::getName).collect(Collectors.toList())).hasSameElementsAs(Arrays.asList(HttpHeaders.AUTHORIZATION, INTEGRASJON_ID, INTEGRASJON_PASSWORD));
        verify(maskinportenklient).getAccessToken(isA(AccessTokenRequest.class));
        verifyNoMoreInteractions(maskinportenklient, request);
    }
}
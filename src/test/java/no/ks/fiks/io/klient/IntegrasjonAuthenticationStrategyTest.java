package no.ks.fiks.io.klient;

import no.ks.fiks.maskinporten.MaskinportenklientOperations;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrasjonAuthenticationStrategyTest {

    @Test
    void setAuthenticationHeaders(@Mock MaskinportenklientOperations maskinportenklient, @Mock Request request) {
        final UUID integrasjonId = UUID.randomUUID();
        final String integrasjonPassord = "passord";
        final String accessToken = "accessToken";

        when(maskinportenklient.getAccessToken(anyString())).thenReturn(accessToken);
        when(request.header(eq(HttpHeader.AUTHORIZATION), anyString())).thenReturn(request);
        when(request.header(eq(IntegrasjonAuthenticationStrategy.INTEGRASJON_ID), anyString())).thenReturn(request);
        when(request.header(eq(IntegrasjonAuthenticationStrategy.INTEGRASJON_PASSWORD), anyString())).thenReturn(request);

        new IntegrasjonAuthenticationStrategy(maskinportenklient, integrasjonId, integrasjonPassord)
                .setAuthenticationHeaders(request);

        verify(maskinportenklient).getAccessToken(anyString());
        verify(request).header(eq(HttpHeader.AUTHORIZATION), anyString());
        verify(request).header(eq(IntegrasjonAuthenticationStrategy.INTEGRASJON_ID), anyString());
        verify(request).header(eq(IntegrasjonAuthenticationStrategy.INTEGRASJON_PASSWORD), anyString());
        verifyNoMoreInteractions(maskinportenklient, request);
    }
}
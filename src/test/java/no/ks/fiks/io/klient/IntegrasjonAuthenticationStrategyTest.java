package no.ks.fiks.io.klient;

import no.ks.fiks.maskinporten.AccessTokenRequest;
import no.ks.fiks.maskinporten.MaskinportenklientOperations;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static no.ks.fiks.io.klient.IntegrasjonAuthenticationStrategy.INTEGRASJON_ID;
import static no.ks.fiks.io.klient.IntegrasjonAuthenticationStrategy.INTEGRASJON_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrasjonAuthenticationStrategyTest {

    @Captor
    private ArgumentCaptor<Consumer<HttpFields.Mutable>> headerCaptor;

    @Test
    void setAuthenticationHeaders(@Mock MaskinportenklientOperations maskinportenklient, @Mock Request request) {
        final UUID integrasjonId = UUID.randomUUID();
        final String integrasjonPassord = "passord";

        when(request.headers(any())).thenReturn(request);
        when(maskinportenklient.getAccessToken(isA(AccessTokenRequest.class))).thenReturn("token");

        new IntegrasjonAuthenticationStrategy(maskinportenklient, integrasjonId, integrasjonPassord)
                .setAuthenticationHeaders(request);

        verify(request).headers(headerCaptor.capture());
        var mutable = new HttpFields.Mutable() {
        };
        headerCaptor.getValue().accept(mutable);
        final Set<String> fieldNamesCollection = mutable.getFieldNamesCollection();
        assertThat(fieldNamesCollection).hasSameElementsAs(Arrays.asList(HttpHeader.AUTHORIZATION.asString(), INTEGRASJON_ID, INTEGRASJON_PASSWORD));
        verify(maskinportenklient).getAccessToken(isA(AccessTokenRequest.class));
        verifyNoMoreInteractions(maskinportenklient, request);
    }
}
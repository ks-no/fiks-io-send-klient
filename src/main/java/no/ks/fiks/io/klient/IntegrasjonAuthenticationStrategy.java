package no.ks.fiks.io.klient;

import no.ks.fiks.maskinporten.AccessTokenRequest;
import no.ks.fiks.maskinporten.MaskinportenklientOperations;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.BasicHeader;

import java.util.UUID;

public class IntegrasjonAuthenticationStrategy implements AuthenticationStrategy {

    static final String INTEGRASJON_ID = "IntegrasjonId";

    static final String INTEGRASJON_PASSWORD = "IntegrasjonPassord";

    private final MaskinportenklientOperations maskinportenklient;
    private final UUID integrasjonId;
    private final String integrasjonPassord;

    public IntegrasjonAuthenticationStrategy(MaskinportenklientOperations maskinportenklient, UUID integrasjonId, String integrasjonPassord) {
        this.maskinportenklient = maskinportenklient;
        this.integrasjonId = integrasjonId;
        this.integrasjonPassord = integrasjonPassord;
    }

    @Override
    public void setAuthenticationHeaders(ClassicHttpRequest request) {
        request.addHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken()));
        request.addHeader(new BasicHeader(INTEGRASJON_ID, integrasjonId.toString()));
        request.addHeader(new BasicHeader(INTEGRASJON_PASSWORD, integrasjonPassord));
    }

    private String getAccessToken() {
        return maskinportenklient.getAccessToken(AccessTokenRequest.builder().scope("ks:fiks").build());
    }
}

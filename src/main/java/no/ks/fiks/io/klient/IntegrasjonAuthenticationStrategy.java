package no.ks.fiks.io.klient;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.message.BasicHeader;

import java.util.UUID;
import java.util.function.Supplier;

public class IntegrasjonAuthenticationStrategy implements AuthenticationStrategy {

    static final String INTEGRASJON_ID = "IntegrasjonId";

    static final String INTEGRASJON_PASSWORD = "IntegrasjonPassord";

    private final Supplier<String> maskinportenTokenSupplier;
    private final UUID integrasjonId;
    private final String integrasjonPassord;

    public IntegrasjonAuthenticationStrategy(Supplier<String> maskinportenTokenSupplier, UUID integrasjonId, String integrasjonPassord) {
        this.maskinportenTokenSupplier = maskinportenTokenSupplier;
        this.integrasjonId = integrasjonId;
        this.integrasjonPassord = integrasjonPassord;
    }

    @Override
    public void setAuthenticationHeaders(ClassicHttpRequest request) {
        request.addHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + maskinportenTokenSupplier.get()));
        request.addHeader(new BasicHeader(INTEGRASJON_ID, integrasjonId.toString()));
        request.addHeader(new BasicHeader(INTEGRASJON_PASSWORD, integrasjonPassord));
    }

}

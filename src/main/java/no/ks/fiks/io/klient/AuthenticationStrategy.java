package no.ks.fiks.io.klient;

import org.apache.hc.core5.http.ClassicHttpRequest;

public interface AuthenticationStrategy {
    void setAuthenticationHeaders(ClassicHttpRequest request);
}

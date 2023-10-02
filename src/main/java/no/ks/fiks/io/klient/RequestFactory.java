package no.ks.fiks.io.klient;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;

/**
 * Factory for nye send requester
 */
public interface RequestFactory {

    /**
     * Oppretter ny {@link ClassicHttpRequest} for å sende til fiks-io
     * @param contentProvider innhold som skal sendes
     * @return en ny {@link ClassicHttpRequest}
     */
    ClassicHttpRequest createSendToFiksIORequest(HttpEntity contentProvider);
}

package no.ks.fiks.io.klient;

import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;

import java.io.Closeable;

/**
 * Factory for nye send requester
 */
public interface RequestFactory extends Closeable {

    /**
     * Oppretter ny {@link Request} for å sende til fiks-io
     * @param contentProvider innhold som skal sendes
     * @return en ny {@link Request}
     */
    Request createSendToFiksIORequest(ContentProvider contentProvider);
}
